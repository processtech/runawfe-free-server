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
            :search="search"
            :footer-props="{
                disablePagination: false,
                disableItemsPerPage: false,
                itemsPerPageAllText: 'Все',
                itemsPerPageText: 'Строк на странице',
            }"
            hide-default-header
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
                <tr>
                    <td>
                        <v-text-field v-model="name" label="Задача" />
                    </td>
                    <td>
                        <v-text-field v-model="description" label="Описание" />
                    </td>
                    <td>
                        <v-text-field v-model="processId" type="number" label="№ экз." />
                    </td>
                    <td>
                        <v-text-field v-model="category" type="text" label="Тип процесса" />
                    </td>
                    <td>
                        <v-text-field v-model="definitionName" label="Процесс" />
                    </td>
                    <td>
                        <v-text-field v-model="creationDate" label="Создана" />
                    </td>
                    <td>
                        <v-text-field v-model="deadlineDate" label="Выполнена" />
                    </td>
                </tr>
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
        search: '',
        name: '',
        description: '',
        processId: '',
        category: '',
        definitionName: '',
        creationDate: '',
        deadlineDate: '',
        tasks: [],
        loading: true,
        options: new Options(),
      }
    },
    computed: {
        headers() {
            return [
                {
                    text: 'Задача',
                    align: 'start',
                    value: 'name',
                    filter: (value: any): boolean => {
                        if (!this.name) return true;
                        return value.indexOf(this.name) !== -1;
                    },
                },
                { 
                    text: 'Описание', 
                    value:'description',
                    filter: (value: any): boolean => {
                        if (!this.description) return true;
                        return value.indexOf(this.description) !== -1;
                    },
                },
                { 
                    text: '№ экз.', 
                    value: 'processId',
                    filter: (value: any): boolean => {
                        if (!this.processId) return true;
                        return value == parseInt(this.processId);
                    },
                },
                { 
                    text: 'Тип процесса', 
                    value: 'category', 
                    sortable: false,
                    filter: (value: any): boolean => {
                        if (!this.category) return true;
                        return value.indexOf(this.category) !== -1;
                    },
                },
                { 
                    text: 'Процесс', 
                    value: 'definitionName',
                    filter: (value: any): boolean => {
                        if (!this.definitionName) return true;
                        return value.indexOf(this.definitionName) !== -1;
                    },
                },
                { 
                    text: 'Создана', 
                    value: 'creationDate' 
                },
                { 
                    text: 'Выполнена', 
                    value: 'deadlineDate' 
                },
            ];
        }
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