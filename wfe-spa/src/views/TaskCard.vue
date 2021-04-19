<template>
    <v-card>
        <v-container fluid fill-height tag="section">
            <v-row justify="center" align="center">
                <v-col cols="12">
                    <v-card>
                        <v-card-title>
                            <v-btn 
                                text 
                                icon 
                                color="grey"
                                @click="goBack"
                                class="mr-2"
                            >
                                <v-icon>mdi-chevron-double-left</v-icon>
                            </v-btn>
                            <div>
                                <div class="mb-2 mt-2">
                                    <h1>{{ $__ucfirst(task.definitionName) }}</h1>
                                </div>
                                <div class="mb-2 mt-2">
                                    Экземпляр процесса № {{ task.processId }}
                                </div>
                                <div class="mb-2 mt-2 grey--text">
                                    <h2>{{ $__ucfirst(task.name) }}</h2>
                                </div>
                            </div>
                            <v-btn 
                                text
                                icon 
                                link
                                class="ml-2"
                                color="grey"
                                @click="showTaskInfo = !showTaskInfo"
                                v-model="showTaskInfo"
                            >
                                <v-icon>mdi-information-outline</v-icon>
                            </v-btn>
                            <v-spacer />
                            <v-btn
                                class="float-right"
                                color="primary"
                                @click="completeTask(task)"
                            >
                            Выполнить
                            </v-btn>
                        </v-card-title>
                    </v-card>
                    <v-expand-transition>
                        <v-card v-show="showTaskInfo" class="mt-4">
                            <v-card-title>
                                Информация об экземпляре:
                            </v-card-title>
                            <v-card-text style="color: rgba(0, 0, 0, 1);">
                                <template v-for="(value, name, index) in task.getTaskInfo()">
                                    <div :key="index" class="mb-1">
                                        <span class="d-inline-block" style="width: 20em">{{ $__ucfirst(name) }}: </span>
                                        <span class="d-inline-block">{{ value }}</span>
                                    </div>
                                </template>
                            </v-card-text>
                        </v-card>
                    </v-expand-transition>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12">
                    <iframe
                        :src="oldFormUrl"
                        width="100%"
                        height="100%"
                        frameborder="0" >
                        Ваш браузер не поддерживает плавающие фреймы!
                    </iframe>
                </v-col>
            </v-row>
        </v-container>
    </v-card>
</template>

<script lang="ts">
import Vue from 'vue';
import { Task } from '../ts/task';
import { get, sync } from 'vuex-pathify';

export default Vue.extend({
    name: "TaskCard",
    data() {
        return {
            showTaskInfo: false,
            task: new Task(),
            oldFormUrl: 'about:blank',
        }
    },
    computed: {
        token: get('user/token'),
    },
    methods: {
        goBack() {
            window.history.length > 1 ? this.$router.go(-1) : this.$router.push({ name: 'Мои задачи' });
        },
        completeTask(task: any) {
            const variables = {};
            this.$apiClient().then((client: any) => {
                client['task-api-controller'].completeTaskUsingPOST(null, { 
                    parameters: {
                        id: task.id
                    },
                    requestBody: variables 
                }).then((data: any) => {
                    if (data.status == 200) {
                        this.$router.push({ name: 'Мои задачи' });
                    }
                });
            });
        },
        loadTask() {
            this.$apiClient().then((client: any) => {
                client['task-api-controller'].getTaskUsingGET(null, { 
                    parameters: {
                        id: this.$route.params.id
                    }
                }).then((data: any) => {
                    if (data) {
                        this.task = Object.assign(this.task, data.body);
                        this.loadForm();
                    }
                });
            });
        },
        loadForm() {
            this.oldFormUrl = `http://localhost:8080/wfe/form-preloader?taskId=${this.task.id}&jwt=${this.token}`;
        }
    },
    created: function() {
        this.loadTask();
    }
});
</script>
