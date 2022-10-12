<template>
    <v-data-table
        class="elevation-1 wfe-process-table"
        :item-class="getItemClass"
        :headers="visibleHeaders"
        :items="records"
        item-key="id"
        :options.sync="options"
        :server-items-length="total"
        :loading="loading"
        :footer-props="{
            disablePagination: false,
            disableItemsPerPage: false,
            itemsPerPageAllText: 'Все',
            itemsPerPageText: 'Строк на странице',
        }"
        >
        <template v-for="header in visibleHeaders" v-slot:[`item.${header.value}`]="{ item }">
            <div v-if="header.value==='start'">
                <v-icon
                    color="green"
                    class="mr-2"
                    :disabled="!item.canBeStarted"
                    @click="openStartForm(item)"
                >
                    mdi-play-circle
                </v-icon>
            </div>
            <div v-else-if="header.dynamic">
                {{ getVariableValue(header.text, item) }}
            </div>
            <div v-else-if="header.format==='DateTime'">
                {{ getDateTime(item[header.value]) }}
            </div>
            <div v-else-if="header.format==='String' && header.link">
                <card-link :routeName="routeName" :id="item.id" :text="item[header.value]" />
            </div>
            <div v-else-if="header.format==='String' && header.selectOptions">
                {{ getValueFromOptions(header.selectOptions, item[header.value]) }}
            </div>
            <div v-else-if="header.format==='Actor'">
                {{ getActor(item[header.value]) }}
            </div>
            <div v-else>
                {{ item[header.value] }}
            </div>
        </template>
        <template v-slot:[`footer.page-text`]="items">
            {{ items.pageStart }} - {{ items.pageStop }} из {{ items.itemsLength }}
        </template>
        <template v-slot:no-data>
            Данные отсутствуют
        </template>
        <template v-slot:[`body.prepend`]>
            <tr v-if="filterVisible" class="filter-row">
                <filter-cell v-for="header in visibleHeaders"
                    :header="header"
                    :key="header.value"
                    v-model="filter[header.value]"
                    @update-filter-event="checkFilter(header)"
                    @update-filter-and-reload-event="checkFilterAndReload(header);"
                />
            </tr>
        </template>
        <template v-slot:top>
            <v-toolbar flat>
                <v-spacer/>
                <v-btn
                    text
                    icon
                    color="rgba(0, 0, 0, 0.67)"
                    @click="reloadData()"
                >
                    <v-icon>{{applyAll ? 'mdi-check-all':'mdi-reload'}}</v-icon>
                </v-btn>
                <v-btn
                    text
                    icon
                    @click="toggleFilterVisible()"
                    v-model="filterVisible"
                    color="rgba(0, 0, 0, 0.67)"
                >
                    <v-icon>mdi-filter</v-icon>
                </v-btn>
                <v-btn
                    text
                    icon
                    :depressed="!filterNow && !applyAll"
                    :disabled="!filterNow && !applyAll"
                    color="rgba(0, 0, 0, 0.67)"
                    @click="clearFilters(); $emit('get-data-event', options, filter, variables);"
                >
                    <v-icon>mdi-close</v-icon>
                </v-btn>
                <columns-visibility :headers="headers" :dynamic="dynamic"
                    @update-data-event="updateData"
                    @toggle-head-visible-event="onToggleHeadVisible"
                    @delete-variable-event="onDeleteVariable"
                    @add-variable-event="onAddVariable"
                />
                <color-description :colors="colors" />
            </v-toolbar>
        </template>
    </v-data-table>
</template>

<script lang="ts">
import Vue from 'vue';
import { PropOptions } from 'vue';
import { get, sync } from 'vuex-pathify';
import { Options, Sorting, Select, Header } from '../ts/Options';

export default Vue.extend({
    name: "WfeTables",
    props: {
        initialHeaders: {
            type: Array
        } as PropOptions<Header[]>,
        records: Array,
        colors: {
            type: Array
        } as PropOptions<{ value: string, desc: string }[]>,
        total: Number,
        loading: Boolean,
        routeName: String,
        prefixLocalStorageName: String,
        dynamic: Boolean,
    },
    data() {
        return {
            options: new Options(),
            headers: [],
            variables: [],
            filter: {},
            filterVisible: false,
            filterNow: false,
            applyAll: false,
            activeFilterColor: '#FFFFE0',
        }
    },
    mounted: function () {
        if (localStorage.getItem(this.prefixLocalStorageName + '-headers')) {
            try {
                this.headers = JSON.parse(localStorage.getItem(this.prefixLocalStorageName + '-headers'));
            } catch(e) {
                localStorage.removeItem(this.prefixLocalStorageName + '-headers');
                this.initHeaders();
            }
        } else {
            this.initHeaders();
        }
        if (localStorage.getItem(this.prefixLocalStorageName + '-variables')) {
            try {
                this.variables = JSON.parse(localStorage.getItem(this.prefixLocalStorageName + '-variables'));
            } catch(e) {
                localStorage.removeItem(this.prefixLocalStorageName + '-variables');
            }
        }
        if (localStorage.getItem(this.prefixLocalStorageName + '-filters')) {
            try {
                this.filter = JSON.parse(localStorage.getItem(this.prefixLocalStorageName + '-filters'));
                this.filterNow = true;
                this.filterVisible = true;
            } catch(e) {
                localStorage.removeItem(this.prefixLocalStorageName + '-filters');
                this.initFilter();
            }
        } else {
            this.initFilter();
        }
    },
    computed: {
        visibleHeaders(): any {
            return this.headers.filter((h: any) => {
                return h.visible;
            });
        },
    },
    watch: {
        options: {
            handler () {
                this.$emit('get-data-event', this.options, this.filter, this.variables);
            },
            deep: true,
        },
    },
    methods: {
        getItemClass (record: any) {
            return record.class;
        },
        getDateTime (value: string) {
            // TODO date.format.pattern, default dd.MM.yyyy HH:mm
            if (!value) return '';
            return new Date(value).toLocaleString("ru", {day: "numeric", month: "numeric", year: "numeric", hour: "numeric", minute: "numeric"}).replace(',','');
        },
        getDate (value: string) {
            // TODO date.format.pattern, default dd.MM.yyyy
            if (!value) return '';
            return new Date(value).toLocaleDateString("ru", {day: "numeric", month: "numeric", year: "numeric"});
        },
        getTime (value: string) {
            if (!value) return '';
            // Time format is always HH:mm
            return new Date(value).toLocaleTimeString("ru", {hour: "numeric", minute: "numeric"});
        },
        getActor (actor) {
            if(actor) {
                if('name' in actor) {
                    return actor.name;
                }
            }
            return '';
        },
        getValueFromOptions (options:Select[], value: string) {
            const result = options.find(o => o.value === value);
            if(result) {
                return result.text;
            } else {
                return '';
            }
        },
        isAnyFilter () {
            for (let prop in this.filter) {
                if (this.filter.hasOwnProperty(prop)) {
                    if (this.filter[prop] !== null && this.filter[prop] !== '') {
                        return true;
                    }
                }
            }
            return false;
        },
        initFilter () {
            for (let h of this.headers) {
                this.filter[h.value] = null;
            }
        },
        initHeaders () {
            this.headers = this.initialHeaders;
            this.variables = [];
            localStorage.setItem(this.prefixLocalStorageName + '-headers', JSON.stringify(this.headers));
            localStorage.setItem(this.prefixLocalStorageName + '-variables', JSON.stringify(this.variables));
        },
        clearHeadersColor () {
            this.visibleHeaders.forEach(header => {
                    header.bcolor = '';
            });
        },
        checkFilterAndReload (header) {
            this.checkFilter(header);
            this.reloadData();
        },
        checkFilter (header) {
            const l = JSON.stringify(this.filter);
            const s = localStorage.getItem(this.prefixLocalStorageName + '-filters');
            const storageFilter = JSON.parse(s);
            if((!s && this.isAnyFilter()) || (s && s!==l)) {
                this.applyAll = true;
            } else {
                this.applyAll = false;
            }
            if((!storageFilter && this.filter[header.value])
                || (storageFilter && storageFilter[header.value]!==this.filter[header.value])) {
                header.bcolor = this.activeFilterColor;
            } else {
                header.bcolor = '';
            }
        },
        updateFiltersInLocalStorage() {
            localStorage.setItem(this.prefixLocalStorageName + '-filters', JSON.stringify(this.filter));
        },
        clearFiltersInLocalStorage() {
            localStorage.removeItem(this.prefixLocalStorageName + '-filters');
        },
        reloadData () {
            if(this.isAnyFilter()) {
                this.clearHeadersColor();
                this.filterNow = true;
                this.updateFiltersInLocalStorage();
            } else {
                this.clearFilters();
            }
            this.applyAll = false;
            this.$emit('get-data-event', this.options, this.filter, this.variables);
        },
        clearFilters () {
            this.applyAll = false;
            this.filterVisible = false;
            this.filterNow = false;
            Object.keys(this.filter).forEach(key => {
                this.filter[key] = null;
            });
            this.clearHeadersColor();
            this.clearFiltersInLocalStorage();
        },
        toggleFilterVisible () {
            if (!this.filterNow && !this.applyAll) {
                this.filterVisible = !this.filterVisible;
            }
        },
        updateData () {
            localStorage.setItem(this.prefixLocalStorageName + '-variables', JSON.stringify(this.variables));
            localStorage.setItem(this.prefixLocalStorageName + '-headers', JSON.stringify(this.headers));
            this.$emit('get-data-event', this.options, this.filter, this.variables);
        },
        getHeadByValue (value) {
            return this.headers.find(h => h.value === value);
        },
        onToggleHeadVisible (id) {
            const h = this.getHeadByValue(id);
            if (!h.visible) {
                this.filter[id] = null;
                if (this.isAnyFilter()) {
                    this.updateFiltersInLocalStorage();
                } else {
                    this.clearFilters();
                }
            }
        },
        onDeleteVariable (variableName) {
            if (variableName) {
                const headerIndx = this.headers.findIndex(h => h.text === variableName);
                if (headerIndx > 0){
                    this.headers.splice(headerIndx, 1);
                    const varIndx = this.variables.indexOf(variableName);
                    this.variables.splice(varIndx, 1);
                    this.filter[variableName] = null;
                    if (this.isAnyFilter()) {
                        this.updateFiltersInLocalStorage();
                    } else {
                        this.clearFilters();
                    }
                }
            }
        },
        onAddVariable (variableName) {
            if (variableName) {
                const headerIndx = this.headers.findIndex(h => h.text === variableName);
                if (headerIndx === -1){
                    let header = new Header();
                    const index = this.variables.length + 1;
                    header.text = variableName;
                    header.align = '';
                    header.value = variableName;
                    header.dynamic = true;
                    header.visible = true;
                    header.width = '10em';
                    header.sortable = false;
                    header.selectOptions = '';
                    this.headers.push(header);
                    this.variables.push(variableName);
                    if (this.isAnyFilter()) {
                        this.updateFiltersInLocalStorage();
                    }
                }
            }
        },
        getVariableValue (variableName, data) {
            for (let variable of data.variables) {
                if (variableName == variable.name ) {
                    const prefix = 'ru.runa.wfe.var.format.';
                    if (!variable.format.includes(prefix)) {
                        let obj = {};
                        obj = Object.assign(obj, variable.value);
                        if (obj && Object.keys(obj).length !== 0) {
                            return variable.value;
                        }
                        return '';
                    }
                    const format = variable.format.replace(prefix,'').replace('Format','');
                    if (format === 'Date') {
                        return this.getDate(variable.value);
                    } else if (format === 'Time') {
                        return this.getTime(variable.value);
                    } else if (format === 'DateTime') {
                        return this.getDateTime(variable.value);
                    } else if (format === 'Actor' || format === 'Executor' || format === 'Group') {
                        let executor = {};
                        executor = Object.assign(executor, variable.value);
                        return executor.name;
                    } else if (format === 'File') {
                        // not support
                        return '';
                    } else if (format.includes('List')) {
                        let arr = [];
                        arr = Object.assign(arr, variable.value);
                        if (arr.length) {
                            return variable.value;
                        }
                        return '';
                    } else if (format.includes('Map')) {
                        let obj = {};
                        obj = Object.assign(obj, variable.value);
                        if (obj && Object.keys(obj).length !== 0) {
                            return variable.value;
                        }
                        return '';
                    } else {
                        return variable.value;
                    }
                }
            }
        },
        openStartForm (item) {
            this.$router.push({ name: this.routeName, params: { id: item.id.toString() } });
        },
    },
});
</script>
