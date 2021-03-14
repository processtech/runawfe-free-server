<template>
    <v-container
        id="regular-tables-view"
        fluid
        tag="section"
    >
        <v-data-table
            :headers="headers"
            :items="tasks"
            :options.sync="options"
            :server-items-length="totalTasks"
            :loading="loading"
            class="elevation-1" />

    </v-container>
</template>

<script lang="ts">
import Vue from 'vue';
import SwaggerClient from 'swagger-client';

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
            // { text: 'Тип процесса', value: 'type' }, // TODO Добавить поле тип процесса в response
            { text: 'Процесс', value: 'definitionName' },
            { text: 'Создана', value: 'creationDate' },
            // { text: 'Выполнена', value: 'dateComplete' }, // TODO Добавить поле тип процесса в response
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

            const client = new SwaggerClient({ 
                url: 'http://localhost:8080/restapi/v3/api-docs',
                disableInterfaces: false,
                v2OperationIdCompatibilityMode: false,
            });
            
            client.then((client: any) => {
                const data = client.apis['test-api-controller'].getTasksUsingGET().then((data: any) => {
                    const tasks = data.body;
                    
                    this.tasks = data.body;
                    this.totalTasks = data.body.length;
                    this.loading = false;
                });
                
            });

        },
    },
});
</script>