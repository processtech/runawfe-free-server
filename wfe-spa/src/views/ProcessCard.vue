<template>
    <v-card>
        <v-form id="process-card-view">
            <v-container fluid fill-height tag="section">
                <v-row justify="center" align="center">
                    <v-col cols="12">
                        <v-card-title>
                            <v-btn 
                                text 
                                icon 
                                color="grey"
                                @click="goBack"
                            >
                                <v-icon>mdi-chevron-double-left</v-icon>
                            </v-btn>
                            <h1>{{ processId }}</h1>
                        </v-card-title>
                    </v-col>
                </v-row>
                <v-row justify="center" align="start">
                    <v-col cols="8">
                        <v-img 
                            :src="graphImage"
                        ></v-img>
                    </v-col>
                    <v-col cols="4"></v-col>
                </v-row>
            </v-container>
        </v-form>
    </v-card>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';

export default Vue.extend({
    name: "ProcessCard",

    data() {
        return {
            graphImage: '',
            process: null,
            processId: null
        }
    },
    methods: {
        goBack() {
            window.history.length > 1 ? this.$router.go(-1) : this.$router.push({ name: 'Запущенные процессы' });
        },
        getGraph() {
            this.$apiClient().then((client: any) => {
                client['process-api-controller'].getProcessGraphUsingPOST(null, { 
                    parameters: {
                        id: this.$route.params.id
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
        },
        loadProcess(): void {
            this.getGraph();
        }
    },
    created() {
        this.loadProcess();
    }
});
</script>
