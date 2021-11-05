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
                                    <h1>{{ $__ucfirst(process.definitionName) }}</h1>
                                </div>
                                <div class="mb-2 mt-2">
                                    Экземпляр процесса № {{ process.id }}
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
                                <template v-for="(value, name, index) in process.getInfo()">
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
            <v-row justify="center" align="start">
                <v-col cols="12">
                    <v-img 
                        max-width="800px"
                        max-height="100%"
                        :src="graphImage"
                    ></v-img>
                </v-col>
            </v-row>
        </v-container>
    </v-card>
</template>

<script lang="ts">
import Vue from 'vue';
import { WfProcessDto } from '../ts/WfProcessDto';
import { get, sync } from 'vuex-pathify';

export default Vue.extend({
    name: "ProcessCard",
    data() {
        return {
            graphImage: '',
            process: new WfProcessDto(),
            showInfo: false
        }
    },
    methods: {
        goBack() {
            window.history.length > 1 ? this.$router.go(-1) : this.$router.push({ name: 'Запущенные процессы' });
        },
        loadProcess(): void {
            this.$apiClient().then((client: any) => {
                client['process-api-controller'].getProcessUsingGET(null, { 
                    parameters: {
                        id: this.$route.params.id
                    }
                }).then((data: any) => {
                    if (data) {
                        this.process = Object.assign(this.process, data.body);
                        this.getGraph();
                    }
                });
            });
        },
        getGraph() {
            this.$apiClient().then((client: any) => {
                client['process-api-controller'].getProcessGraphUsingPOST(null, { 
                    parameters: {
                        id: this.process.id
                    },
                    requestBody: { 
                        childProcessId: null,
                        subprocessId: null,
                    }
                }).then((data: any) => {
                    if (data && data.status == 200) {
                        this.graphImage = 'data:image/jpeg;base64,' + data.body;
                    }
                });
            });
        }
    },
    created() {
        this.loadProcess();
    }
});
</script>
