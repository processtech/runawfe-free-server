<template>
    <v-card>
        <v-form id="task-card-view">
            <v-container fluid fill-height tag="section">
                <v-row justify="center" align="center">
                    <v-col cols="9">
                        <v-card-title>
                            <v-btn 
                                text 
                                icon 
                                color="grey"
                                @click="goBack"
                            >
                                <v-icon>mdi-chevron-double-left</v-icon>
                            </v-btn>
                            <h1>{{ task.name }}</h1>
                            <v-btn 
                                text
                                icon 
                                link
                                color="grey"
                                @click="showTaskInfo = !showTaskInfo"
                                v-model="showTaskInfo"
                            >
                                <v-icon>mdi-information-outline</v-icon>
                            </v-btn>
                        </v-card-title>
                    </v-col>
                    <v-col cols="3">
                        <v-btn
                            class="float-right"
                            color="primary"
                            @click="completeTask(task)"
                        >
                        Выполнить
                        </v-btn>
                    </v-col>
                </v-row>
                <v-row v-if="showTaskInfo" justify="center" align="center">
                    <v-col cols="12">
                        <ul>
                            <li v-for="(value, name, index) in task" :key="index">
                                {{ value }}
                            </li>
                        </ul>
                    </v-col>
                </v-row>
            </v-container>
        </v-form>
    </v-card>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';

export default Vue.extend({
    name: "TaskCard",
    data() {
        return {
            showTaskInfo: false,
        }
    },
    computed: {
        task: get('app/task'),
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
    }
});
</script>
