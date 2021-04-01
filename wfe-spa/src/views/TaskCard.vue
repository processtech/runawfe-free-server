<template>
    <v-form
        ref="form"
        v-model="valid"
        lazy-validation
    >
        <v-btn
            color="primary"
            @click="complete(taskId)"
        >
        Выполнить
        </v-btn>
        <!-- <v-btn
            color="error"
            @click="cancel"
        >
        Отменить
        </v-btn> -->
    </v-form>
</template>

<script lang="ts">
import Vue from 'vue';
import SwaggerClient from 'swagger-client';
import { get, sync } from 'vuex-pathify';
import { Options, Sorting } from '../ts/options';

export default Vue.extend({
    name: "TaskCard",

    data() {
        return {
            valid: true,

        }
    },
    computed: {
        taskId: get('app/task@id'),
    },
    methods: {
        complete(taskId: number) {
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
                        const variables = {};
                        const data = client.apis['task-api-controller'].completeTaskUsingPOST( 
                            null, 
                            { 
                                parameters: {
                                    id: taskId   
                                },
                                requestBody: variables 
                            } 
                        ).then((data: any) => {
                            const body = data.body;
                            if (body) {
                                //TODO Пока сервис не возвращает ничего
                            }
                            
                        }, (reason: any) => {
                            const error = reason.response;
                            if (error.status == 500) {
                                console.log('internal server error');
                            }
                            //TODO логирование ошибок
                        });
                    },
                    (reason: string) => console.error('failed on api call: ' + reason));
                },
                (reason: string) => console.error('failed on api call: ' + reason));
            },
            (reason: string) => console.error('failed to load the spec: ' + reason));
        },
    }
});
</script>
