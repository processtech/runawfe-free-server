<template>
    <v-container
        id="processes-definition-view"
        fluid
        tag="section"
    >
        <v-data-table
            class="elevation-1 wfe-process-table"
            :headers="headers"
            :items="definitions"
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
            <template v-slot:[`item.createActor`]="{ item }">
                {{ item.createActor ? item.createActor.name : '' }}
            </template>
            <template v-slot:[`item.updateActor`]="{ item }">
                {{ item.updateActor ? item.updateActor.name : '' }}
            </template>
            <template v-slot:[`item.createDate`]="{ item }">
                {{ new Date(item.createDate).toLocaleString() }}
            </template>
            <template v-slot:[`item.updateDate`]="{ item }">
                {{ item.updateDate ? new Date(item.updateDate).toLocaleString() : '' }}
            </template>
            <template v-slot:[`item.start`]="{ item }">
                <v-icon
                    color="rgba(0, 0, 0, 0.67)"
                    class="mr-2"
                    @click="startProcess(item)"
                >
                    mdi-play-circle
                </v-icon>
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
                            v-if="header.value != 'start'"
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
                    <columns-visibility :initialHeaders="initialHeaders" />
                </v-toolbar>
            </template>
        </v-data-table>
        <v-snackbar
            v-model="hasWarnings"
            :timeout="timeout"
            absolute
            top
            color="blue-grey"
            rounded="pill"
        >
            {{ warnings }}
            <template v-slot:action="{ attrs }">
                <v-btn
                    text 
                    icon 
                    v-bind="attrs"
                    @click="hasWarnings = false"
                >
                    <v-icon>mdi-close</v-icon>
                </v-btn>
            </template>
        </v-snackbar>
    </v-container>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';
import { Options, Sorting } from '../ts/options';

export default Vue.extend({
    name: "ProcessDefinitionList",
    
    data() {
        return {
            timeout: 10000,
            warnings: '',
            hasWarnings: false,
            dialog: false,
            filterVisible: false,
            filter: {
                name: null,
                // category: '',
                description: null,
                createDate: null,
                createActor: null,
                updateDate: null,
                updateActor: null,
            },
            total: 0,
            definitions: [],
            loading: true,
            options: new Options(),
            initialHeaders: [
                {   
                    text: 'Запустить', 
                    value: 'start',
                    align: 'center',
                    visible: true,
                    sortable: false,
                    width: '1px'
                },
                {
                    text: 'Имя',
                    align: 'start', 
                    value: 'name',
                    visible: true,
                    width: '20em',
                },
                { 
                    text: 'Описание', 
                    value:'description',
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
                    text: 'Дата загрузки', 
                    value: 'createDate',
                    visible: true,
                    width: '12em',
                },
                { 
                    text: 'Автор загрузки', 
                    value: 'createActor',
                    visible: true,
                    sortable: false,
                    width: '12em',
                },
                { 
                    text: 'Дата обновления', 
                    value: 'updateDate',
                    visible: true,
                    width: '12em',
                },
                { 
                    text: 'Автор обновления', 
                    value: 'updateActor',
                    visible: true,
                    sortable: false,
                    width: '12em',
                }
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
        startProcess (process: any) {
            this.hasWarnings = false;
            this.$apiClient().then((client: any) => {
                const variables = {};
                client['process-api-controller'].startUsingPOST(null, {
                    parameters: {
                        id: process.versionId
                    },
                    requestBody: variables 
                }).then((data: any) => {
                    if (data.status == 200 && data.body) {
                        this.warnings = `Экземпляр процесса № ${data.body} запущен`;
                        this.hasWarnings = true;
                    }
                });
            });
        },
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
                client['process-api-controller'].getDefinitionsUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.definitions = body.definitions;
                        this.total = body.total;
                    }
                    this.loading = false;
                });
            });
        },
    },
});
</script>