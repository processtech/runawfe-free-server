<template>
    <v-container
        id="processes-view"
        fluid
        tag="section"
    >
        <v-data-table
            class="elevation-1 wfe-process-table"
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
            <template v-slot:[`item.startDate`]="{ item }">
                {{ new Date(item.startDate).toLocaleString() }}
            </template>
            <template v-slot:[`item.endDate`]="{ item }">
                {{ new Date(item.endDate).toLocaleString() }}
            </template>
            <template v-slot:[`footer.page-text`]="items">
                {{ items.pageStart }} - {{ items.pageStop }} из {{ items.itemsLength }}
            </template>
            <template v-slot:[`item.name`]="{ item }">
                <card-link :routeName="`Карточка процесса`" :id="item.id" :text="item.name" />
            </template>
            <template v-slot:no-data>
                Данные отсутствуют
            </template>
            <template v-slot:[`body.prepend`]>
                <tr v-if="filterVisible">
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
                        @click="filterVisible = !filterVisible" 
                        v-model="filterVisible" 
                        color="rgba(0, 0, 0, 0.67)"
                    >
                        <v-icon>mdi-filter</v-icon>
                    </v-btn>
                    <v-dialog v-model="dialog" max-width="500px">
                        <template v-slot:activator="{ on, attrs }">
                            <v-btn
                                text 
                                icon
                                v-bind="attrs"
                                v-on="on"
                                color="rgba(0, 0, 0, 0.67)"
                            >
                                <v-icon>mdi-view-grid-plus</v-icon>
                            </v-btn>
                        </template>
                        <v-card>
                            <v-card-title>
                                <span class="headline">Настройка вида</span>
                            </v-card-title>
                            <v-card-text>
                                <v-container>
                                    <v-row>
                                        <v-col v-for="header in initialHeaders" :key="header.value" cols="12" sm="6" md="4"> 
                                            <v-checkbox 
                                                v-model="header.visible" 
                                                :label="header.text"
                                                @change="initialHeaders" 
                                            />
                                        </v-col>
                                    </v-row>
                                </v-container>
                            </v-card-text>
                        </v-card>
                    </v-dialog>
                </v-toolbar>
            </template>
        </v-data-table>
    </v-container>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';
import { Options, Sorting } from '../ts/options';

export default Vue.extend({
    name: "ProcessList",

    data() {
        return {
            dialog: false,
            filterVisible: false,
            filter: {
                id: null,
                name: null,
                // category: '',
                executionStatus: null,
                startDate: null,
                endDate: null,
            },
            total: 0,
            processes: [],
            loading: true,
            options: new Options(),
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
                    value:'name',
                    visible: true,
                    width: '20em',
                },
                // {   
                //     text: 'Тип процесса', 
                //     value: 'category',
                //     visible: false,
                //     width: '20em',
                // },
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
        getDataFromApi () {
            this.loading = true;
            const { page, itemsPerPage, sortBy, sortDesc } = this.options;
            const query = {
                filters: this.filter,
                pageNumber: page,
                pageSize: itemsPerPage,
                sortings: Sorting.convert(sortBy, sortDesc),
                variables: []
            };
            this.$apiClient().then((client: any) => {
                client['process-api-controller'].getProcessesUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.processes = body.processes;
                        this.total = body.total;
                    }
                    this.loading = false;
                });
            });
        },
    },
});
</script>