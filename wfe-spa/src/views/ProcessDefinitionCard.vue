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
                                    <h1>{{ $__ucfirst(definition.name) }}</h1>
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
                                Информация об определении процесса:
                            </v-card-title>
                            <v-card-text style="color: rgba(0, 0, 0, 1);">
                                <template v-for="(value, name, index) in definition.getInfo()">
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
import { WfDefinitionDto } from '../ts/WfDefinitionDto';
import { get, sync } from 'vuex-pathify';

export default Vue.extend({
    name: "ProcessDefinitionCard",
    data() {
        return {
            showInfo: false,
            definition: new WfDefinitionDto(),
            oldFormUrl: 'about:blank',
        }
    },
    computed: {
        token: get('user/token'),
    },
    methods: {
        goBack() {
            this.$router.push({ name: 'Определения процессов' });
        },
        loadDefinition() {
            this.$apiClient().then((client: any) => {
                client['process-definition-api-controller'].getDefinitionByVersionIdUsingGET(null, { 
                    parameters: {
                        versionId: this.$route.params.versionId
                    }
                }).then((data: any) => {
                    if (data) {
                        this.definition = Object.assign(this.definition, data.body);
                        this.oldFormUrl = `http://localhost:8080/wfe/newweboldform.do?id=${this.definition.versionId}&jwt=${this.token}&startForm=true`;
                    }
                });
            });
        }
    },
    created: function() {
        this.loadDefinition();
    }
});
</script>
