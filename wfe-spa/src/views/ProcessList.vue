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
                <div v-else-if="header.value==='startDate' || header.value==='endDate'">
                    {{ new Date(item[header.value]).toLocaleString() }}
                </div>
                <div v-else-if="header.value==='definitionName'">
                    <card-link :routeName="`Карточка процесса`" :id="item.id" :text="item.definitionName" />
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
                    <td v-for="header in headers" :key="header.value">
                        <v-text-field 
                            color="primary"
                            v-model="filter[header.value]" 
                            dense 
                            outlined 
                            clearable 
                            hide-details
                        />
                    </td>
                </tr>
            </template>
            <template v-slot:top>
                <v-toolbar flat>
                    <v-spacer/>
                    <v-btn 
                        text 
                        icon
                        color="rgba(0, 0, 0, 0.67)"
                         @click="getDataFromApi()"
                    >
                        <v-icon>mdi-reload</v-icon>
                    </v-btn>
                    <v-btn 
                        text 
                        icon 
                        @click="filterVisible = !filterVisible" 
                        v-model="filterVisible" 
                        color="rgba(0, 0, 0, 0.67)"
                    >
                        <v-icon>mdi-filter</v-icon>
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
import { Options, Sorting } from '../ts/Options';

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
                },
                { 
                    text: 'Процесс', 
                    value: 'definitionName',
                    visible: true,
                    width: '20em',
                },
                { 
                    text: 'Статус', 
                    value: 'executionStatus',
                    visible: true,
                    width: '20em',
                },
                { 
                    text: 'Запущен', 
                    value: 'startDate',
                    visible: true,
                    sortable: false,
                    width: '10em',
                },
                { 
                    text: 'Окончен', 
                    value: 'endDate',
                    visible: true,
                    width: '10em',
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
        filter: {
            handler () {
                this.getDataFromApi()
            },
            deep: true,
        }
    },
    methods: {
        updateData () {
            this.getDataFromApi();
            localStorage.setItem('runawfe@process-list-variables', JSON.stringify(this.variables));
            localStorage.setItem('runawfe@process-list-initialHeaders', JSON.stringify(this.initialHeaders));
        },
        getVariableValue(variableName, data) {
            for (let variable of data.variables) {
                if (variableName == variable.name ) {
                    const format = variable.format.replace('ru.runa.wfe.var.format.','');
                    if (format === 'DateFormat') {
                        return new Date(variable.value).toLocaleString();
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
                filters: this.filter,
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