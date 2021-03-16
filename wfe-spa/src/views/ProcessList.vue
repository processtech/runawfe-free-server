<template>
    <v-container
        id="regular-tables-view"
        fluid
        tag="section"
    >
        <v-data-table
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
            class="elevation-1">
            <template v-slot:[`item.creationDate`]="{ item }">
                {{ new Date(item.creationDate).toLocaleString() }}
            </template>
            <template v-slot:[`item.deadlineDate`]="{ item }">
                {{ new Date(item.deadlineDate).toLocaleString() }}
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
    name: "ProcessList",

    data() {
      return {
        total: 0,
        processes: [],
        loading: true,
        options: new Options(),
        headers: [
            { text: '№ экз.', align: 'start', value: 'id' },
            { text: 'Процесс', value: 'name' },
            // { text: 'Тип процесса', value: 'category' }, // TODO Добавить поле "тип процесса"
            { text: 'Статус', value: 'executionStatus' },
            { text: 'Запущен', value: 'startDate' },
            { text: 'Окончен', value: 'endDate' }, 
        ],
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
                        const data = client.apis['process-api-controller'].getProcessesUsingPOST(null, { requestBody: query }).then((data: any) => {
                            const body = data.body;
                            if (body) {
                                this.processes = body.processes;
                                this.total = body.total;
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