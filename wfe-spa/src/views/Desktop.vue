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
            :server-items-length="totalTasks"
            :loading="loading"
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

export default Vue.extend({
    name: "Desktop",

    data() {
      return {
        totalTasks: 0,
        tasks: [],
        loading: true,
        options: {},
        headers: [
            {
                text: 'Задача',
                align: 'start',
                value: 'name',
            },
            { text: '№ экз.', value: 'processId' },
            // { text: 'Тип процесса', value: 'category' }, // TODO Добавить поле "тип процесса"
            { text: 'Процесс', value: 'definitionName' },
            { text: 'Создана', value: 'creationDate' },
            { text: 'Выполнена', value: 'deadlineDate' }, 
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
                        const data = client.apis['task-api-controller'].getTasksUsingPOST().then((data: any) => {
                            const tasks = data.body;
                            
                            this.tasks = data.body;
                            this.totalTasks = data.body.length;
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