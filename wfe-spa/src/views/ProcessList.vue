<template>
    <v-container
        id="processes-view"
        fluid
        tag="section"
    >
        <v-data-table
            class="elevation-1 wfe-process-table"
            :item-class="getClass"
            :headers="headers"
            :items="processes"
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
            <template v-for="header in headers" v-slot:[`item.${header.value}`]="{ item }">
                <div v-if="header.dynamic">
                    {{ getVariableValue(header.text, item) }}
                </div>
                <div v-else-if="header.format==='DateTime'">
                    {{ getDateTime(item[header.value]) }}
                </div>
                <div v-else-if="header.format==='String' && header.link">
                    <card-link :routeName="`Карточка процесса`" :id="item.id" :text="item[header.value]" />
                </div>
                <div v-else-if="header.format==='String' && header.selectOptions">
                    {{ getValueFromOptions(header.selectOptions, item[header.value]) }}
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
                    <filter-cell v-for="header in headers"
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
                        @click="clearFilters(); getDataFromApi()"
                    >
                        <v-icon>mdi-close</v-icon>
                    </v-btn>
                    <columns-visibility :initialHeaders="initialHeaders" :variables="variables" :filter="filter" @update-data-event="updateData"/>
                    <color-description :colors="colors" />
                </v-toolbar>
            </template>
        </v-data-table>
    </v-container>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';
import { Options, Sorting, Select } from '../ts/Options';

export default Vue.extend({
    name: "ProcessList",

    data() {
        return {
            dialog: false,
            filterVisible: false,
            filter: {
                id: null,
                definitionName: null,
                executionStatus: null,
                startDate: null,
                endDate: null
            },
            variables: [],
            total: 0,
            processes: [],
            loading: true,
            options: new Options(),
            filterNow: false,
            applyAll: false,
            activeFilterColor: '#FFFFE0',
            colors: [
                {
                    value: 'process1',
                    desc: 'Установленный срок окончания процесса подходит к концу'
                },
                {
                    value: 'process2',
                    desc: 'Процесс не завершён в установленный срок'
                },
            ],
            initialHeaders: [
                {
                    text: '№ экз.',
                    align: 'start',
                    value: 'id',
                    visible: true,
                    width: '3em',
                    bcolor: '',
                    format: 'Long',
                },
                {
                    text: 'Процесс',
                    value: 'definitionName',
                    visible: true,
                    width: '20em',
                    bcolor: '',
                    format: 'String',
                    link: true,
                },
                {
                    text: 'Статус',
                    value: 'executionStatus',
                    visible: true,
                    width: '20em',
                    bcolor: '',
                    format: 'String',
                    selectOptions:[new Select('Активен','ACTIVE'), new Select('Завершен','ENDED'),
                                   new Select('Приостановлен','SUSPENDED'), new Select('Имеет ошибки выполнения','FAILED')],
                },
                {
                    text: 'Запущен',
                    value: 'startDate',
                    visible: true,
                    sortable: false,
                    width: '10em',
                    bcolor: '',
                    format: 'DateTime',
                },
                {
                    text: 'Окончен',
                    value: 'endDate',
                    visible: true,
                    width: '10em',
                    bcolor: '',
                    format: 'DateTime',
                },
            ]
        }
    },
    mounted() {
        if (localStorage.getItem('runawfe@process-list-variables')) {
            try {
                this.variables = JSON.parse(localStorage.getItem('runawfe@process-list-variables'));
            } catch(e) {
                localStorage.removeItem('runawfe@process-list-variables');
            }
        }
        if (localStorage.getItem('runawfe@process-list-initialHeaders')) {
            try {
                this.initialHeaders = JSON.parse(localStorage.getItem('runawfe@process-list-initialHeaders'));
            } catch(e) {
                localStorage.removeItem('runawfe@process-list-initialHeaders');
            }
        }
        if (localStorage.getItem('runawfe@process-list-filters')) {
            try {
                this.filter = JSON.parse(localStorage.getItem('runawfe@process-list-filters'));
                this.filterNow = true;
                this.filterVisible = true;
            } catch(e) {
                localStorage.removeItem('runawfe@process-list-filters');
            }
        }
    },
    computed: {
        headers(): any {
            return this.initialHeaders.filter((h: any) => {
                return h.visible;
            });
        },
    },
    watch: {
        options: {
            handler () {
                this.getDataFromApi()
            },
            deep: true,
        },
    },
    methods: {
        toggleFilterVisible () {
            if (!this.filterNow && !this.applyAll) {
                this.filterVisible = !this.filterVisible;
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
        getFilters (filter) {
            let result = Object.assign({}, filter);
            for (let prop in filter) {
                if (filter.hasOwnProperty(prop)) {
                    if (filter[prop] !== null && filter[prop] !== '') {
                        let header = this.initialHeaders.find(h => h.value === prop);
                        if(!header || (header.format !== 'DateTime' && header.format !== 'Long' && !header.selectOptions)) {
                            result[prop] = '*' + filter[prop].trim() + '*/i';
                        }
                    }
                }
            }
            return result;
        },
        updateFiltersInLocalStorage() {
            localStorage.setItem('runawfe@process-list-filters', JSON.stringify(this.filter));
        },
        clearFiltersInLocalStorage() {
            localStorage.removeItem('runawfe@process-list-filters');
        },
        clearHeadersColor () {
            this.headers.forEach(header => {
                    header.bcolor = '';
            });
        },
        checkFilterAndReload (header) {
            this.checkFilter(header);
            this.reloadData();
        },
        checkFilter (header) {
            const l = JSON.stringify(this.filter);
            const s = localStorage.getItem('runawfe@process-list-filters');
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
        reloadData () {
            if(this.isAnyFilter()) {
                this.clearHeadersColor();
                this.filterNow = true;
                this.updateFiltersInLocalStorage();
            } else {
                this.clearFilters();
            }
            this.applyAll = false;
            this.getDataFromApi();
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
        updateData () {
            this.getDataFromApi();
            localStorage.setItem('runawfe@process-list-variables', JSON.stringify(this.variables));
            localStorage.setItem('runawfe@process-list-initialHeaders', JSON.stringify(this.initialHeaders));
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
        getValueFromOptions(options:Select[], value: string) {
            const result = options.find(o => o.value === value);
            if(result) {
                return result.text;
            } else {
                return '';
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
        getClass (process: any) {
            let cl = '';
            const timestamp = new Date().getTime();
            if (process.endDate != null && process.endDate > timestamp) {
                cl = 'process2';
            }
            return cl;
        },
        getDataFromApi () {
            this.loading = true;
            const { page, itemsPerPage, sortBy, sortDesc } = this.options;
            const query = {
                filters: this.getFilters(this.filter),
                pageNumber: page,
                pageSize: itemsPerPage,
                sortings: Sorting.convert(sortBy, sortDesc),
                variables: this.variables
            };
            this.$apiClient().then((client: any) => {
                client['process-api-controller'].getProcessesUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.processes = body.data;
                        this.total = body.total;
                    }
                    this.loading = false;
                }).catch((error: any) => {
                    this.loading = false;
                    this.processes = [];
                    this.total = 0;
                });
            });
        },
    },
});
</script>