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
                                @click="showInfo = !showInfo"
                                v-model="showInfo"
                            >
                                <v-icon>mdi-information-outline</v-icon>
                            </v-btn>
                        </v-card-title>
                    </v-card>
                    <v-expand-transition>
                        <v-card v-show="showInfo" class="mt-4">
                            <v-card-title>
                                Информация об экземпляре:
                            </v-card-title>
                            <v-card-text style="color: rgba(0, 0, 0, 1);">
                                <template v-for="(value, name, index) in task.getInfo()">
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
                        frameborder="0"
                        style="min-height: 317px;">
                        Ваш браузер не поддерживает плавающие фреймы!
                    </iframe>
                </v-col>
            </v-row>
        </v-container>
    </v-card>
</template>

<script lang="ts">
import Vue from 'vue';
import { WfTaskDto } from '../ts/WfTaskDto';
import { get, sync } from 'vuex-pathify';

export default Vue.extend({
    name: "TaskCard",
    data() {
        return {
            showInfo: false,
            task: new WfTaskDto(),
            oldFormUrl: 'about:blank',
        }
    },
    computed: {
        token: get('user/token'),
    },
    methods: {
        goBack() {
            this.$router.push({ name: 'Мои задачи' });
        },
        loadTask() {
            this.$apiClient().then((client: any) => {
                client['task-controller'].getTaskUsingGET(null, { 
                    parameters: {
                        id: this.$route.params.id
                    }
                }).then((data: any) => {
                    if (data) {
                        this.task = Object.assign(this.task, data.body);
                        this.oldFormUrl = `/wfe/newweboldform.do?id=${this.task.id}&jwt=${this.token}&startForm=false`;
                    }
                });
            });
        }
    },
    created: function() {
        this.loadTask();
    }
});
</script>
