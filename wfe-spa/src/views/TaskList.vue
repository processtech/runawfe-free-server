<template>
    <v-container
        id="regular-tables-view"
        fluid
        tag="section"
    >
        <v-data-table
            :headers="headers"
            :items="tasks"
            item-key="id"
            :options.sync="options"
            :loading="loading"
            :search="filter.search"
            :footer-props="{
                disablePagination: false,
                disableItemsPerPage: false,
                itemsPerPageAllText: 'Все',
                itemsPerPageText: 'Строк на странице',
            }"
            class="elevation-1">
            <template v-slot:[`item.creationDate`]="{ item }">
                {{ new Date(item.creationDate).toLocaleString() }}
            </template>
            <template v-slot:[`item.deadlineDate`]="{ item }">
                {{ new Date(item.deadlineDate).toLocaleString() }}
            </template>
            <template v-slot:[`footer.page-text`]="items">
                {{ items.pageStart }} - {{ items.pageStop }} из {{ items.itemsLength }}
            </template>
            <template v-slot:[`body.prepend`]>
                <tr v-if="filter.visible">
                    <td v-for="header in headers" :key="header.value">
                        <v-text-field 
                            color="grey" 
                            v-model="filter[header.value]" 
                            filled
                            rounded
                            dense 
                            clearable 
                            hide-details
                        />
                    </td>
                </tr>
            </template>
            <template v-slot:[`item.name`]="{ item }">
                <card-link :routeName="`Карточка задачи`" :id="item.id" :text="item.name" />
            </template>
            <template v-slot:[`item.definitionName`]="{ item }">
                <card-link :routeName="`Карточка процесса`" :id="item.id" :text="item.definitionName" />
            </template>
            <template v-slot:top>
                <v-toolbar flat>
                    <v-spacer/>
                    <v-btn 
                        text 
                        icon 
                        @click="filter.visible = !filter.visible" 
                        v-model="filter.visible" 
                        color="grey"
                    >
                        <v-icon >mdi-filter</v-icon>
                    </v-btn>
                    <v-dialog v-model="dialog" max-width="500px">
                        <template v-slot:activator="{ on, attrs }">
                            <v-btn
                                text 
                                icon
                                v-bind="attrs"
                                v-on="on"
                                color="grey"
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
import SwaggerClient from 'swagger-client';
import { get, sync } from 'vuex-pathify';
import { Options, Sorting } from '../ts/options';

export default Vue.extend({
    name: "TaskList",

    data() {
        return {
            dialog: false,
            filter: {
                visible: true,
                search: '',
                name: '',
                description: '',
                processId: '',
                category: '',
                definitionName: '',
                creationDate: '',
                deadlineDate: '',
            },
            tasks: [],
            loading: true,
            options: new Options(),
            initialHeaders: [
                {
                    text: 'Задача',
                    align: 'start', 
                    value: 'name',
                    visible: true,
                    // width: 20,
                },
                { 
                    text: 'Описание', 
                    value:'description',
                    visible: false,
                },
                { 
                    text: '№ экз.', 
                    value: 'processId',
                    visible: true,
                    // width: 10,
                },
                { 
                    text: 'Тип процесса', 
                    value: 'category',
                    visible: false,
                    sortable: false,
                    // width: '10%',
                },
                { 
                    text: 'Процесс', 
                    value: 'definitionName',
                    visible: true,
                    // width: 20,
                },
                { 
                    text: 'Создана', 
                    value: 'creationDate',
                    visible: true,
                    // width: 10,
                },
                { 
                    text: 'Выполнена', 
                    value: 'deadlineDate',
                    visible: true,
                    // width: 10,
                },
            ]
        }
    },
    computed: {
        headers(): any {
            this.initialHeaders.forEach((h: any) => {
                if (h.value === 'name') {
                    h.filter = (value: string): boolean => {
                        if (!this.filter.name) return true;
                        return value.toLowerCase().indexOf(this.filter.name.toLowerCase()) !== -1;
                    };
                } else if (h.value === 'description') {
                    h.filter = (value: string): boolean => {
                        if (!this.filter.description) return true;
                        return value.toLowerCase().indexOf(this.filter.description.toLowerCase()) !== -1;
                    };
                } else if (h.value === 'processId') {
                    h.filter = (value: number): boolean => {
                        if (!this.filter.processId) return true;
                        return value == parseInt(this.filter.processId);
                    };
                } else if (h.value === 'category') {
                    h.filter = (value: string): boolean => {
                        if (!this.filter.category) return true;
                        return value.toLowerCase().indexOf(this.filter.category.toLowerCase()) !== -1;
                    };
                } else if (h.value === 'definitionName') {
                    h.filter = (value: string): boolean => {
                        if (!this.filter.definitionName) return true;
                        return value.toLowerCase().indexOf(this.filter.definitionName.toLowerCase()) !== -1;
                    };
                } else if (h.value === 'creationDate') {

                } else if (h.value === 'deadlineDate') {

                }
            });
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
        getDataFromApi () {
            this.loading = true;

            new SwaggerClient({
                url: 'http://localhost:8080/restapi/v3/api-docs',
            }).then((client: any) => {
                const data = client.apis['auth-controller'].tokenUsingPOST({
                    login: "Administrator",
                    password: "wf"
                }).then((data: any) => {
                    let token = data.body;
                    token = token.split(' ')[1];
                    
                    const client = new SwaggerClient({ 
                        url: 'http://localhost:8080/restapi/v3/api-docs',
                        authorizations: {
                            token: {
                                value: token,
                            },
                        },
                    });
                    
                    // TODO Temprorary for test
                    client.then((client: any) => {
                        const { page, itemsPerPage, sortBy, sortDesc } = this.options;
                        const query = {
                            filters: {},
                            pageNumber: page,
                            pageSize: itemsPerPage,
                            sortings: Sorting.convert(sortBy, sortDesc),
                            variables: []
                        };
                        const data = client.apis['task-api-controller'].getTasksUsingPOST(null, { requestBody: query }).then((data: any) => {
                            const body = data.body;
                            if (body) {
                                this.tasks = body.tasks;
                            }
                            this.loading = false;
                        });
                    },
                    (reason: string) => console.error('failed on api call: ' + reason));
                },
                (reason: string) => console.error('failed on api call: ' + reason));
            },
            (reason: string) => console.error('failed to load the spec: ' + reason));

        },
    },
});
</script>