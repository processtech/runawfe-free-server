<template>
    <v-card>
        <v-container fluid fill-height tag="section">
            <v-row align="center">
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
                                    <h1>{{ $__ucfirst(report.name) }}</h1>
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
                                Информация об отчете:
                            </v-card-title>
                            <v-card-text style="color: rgba(0, 0, 0, 1);">
                                <template v-for="(value, name, index) in report.getInfo()">
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
                <v-alert
                    v-model="messageAlert"
                    dense
                    outlined
                    type="error"
                    dismissible
                >
                    <span>{{ buildResult.message }}</span>
                </v-alert>
            </v-row>
            <v-row>
                <v-col cols="4">
                    <v-select
                        v-model="reportFormat"
                        :items="reportFormats"
                        item-text="str"
                        item-value="abbr"
                        label="Select"
                        persistent-hint
                        return-object
                        single-line
                    ></v-select>
                </v-col>
            </v-row>
            <v-row v-for="parameter in report.parameters" :key="parameter.internalName" align="center" no-gutters>
                <ReportParameters v-bind:parameter="parameter"/>
            </v-row>
            <v-row>
                <v-col cols="12">
                    <v-btn
                      @click="build"
                    >
                        Получить отчет
                    </v-btn>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12">
                    <p><span v-html="buildResult.reportData"></span></p>
                </v-col>
            </v-row>
        </v-container>
    </v-card>
</template>

<script lang="ts">
import Vue from 'vue';
import { WfReportDto } from '../ts/WfReportDto';
import ReportParameters from './ReportParameters.vue';
import { WfReportBuildResultDto } from '../ts/WfReportBuildResultDto';
import { WfReportDataParameter } from '../ts/WfReportDataParameter';
import { get, sync } from 'vuex-pathify';

export default Vue.extend({
    name: "ReportCard",
    components:{
        ReportParameters
    },
    data() {
        return {
            showInfo: false,
            report: new WfReportDto(),
            buildResult: new WfReportBuildResultDto(),
            messageAlert: false,
            reportFormat: { str: 'Показать отчет на странице', abbr: 'HTML_EMBEDDED' },
            reportFormats: [
                { str: 'Показать отчет на странице', abbr: 'HTML_EMBEDDED' },
                { str: 'Выгрузить отчет в формате MS Word', abbr: 'DOCX' },
                { str: 'Выгрузить отчет в формате MS Excel', abbr: 'EXCEL' },
                { str: 'Выгрузить отчет в формате RTF', abbr: 'RTF' },
                { str: 'Выгрузить отчет в формате PDF', abbr: 'PDF' },
            ]
        }
    },
    methods: {
        goBack() {
            this.$router.push({ name: 'Отчеты' });
        },
        build(){
            let parametersData = new Array<WfReportDataParameter>();
            this.report.parameters.forEach(param => {
                const data = new WfReportDataParameter();
                data.internalName = param.internalName;
                data.value = param.value;
                parametersData.push(data);
            });
            this.$apiClient().then((client: any) => {
                client['report-api-controller'].buildReportUsingPOST(null, {
                    parameters: {
                        id: this.$route.params.id
                    },
                    requestBody: {
                        format: this.reportFormat.abbr,
                        parameters: parametersData
                    }
                }).then((data: any) => {
                    if (data && data.status == 200) {
                        this.buildResult = Object.assign(this.buildResult, data.body);
                        if( this.buildResult.message != null){
                            this.messageAlert = true;
                        } else {
                            this.messageAlert = false;
                        }
                        if(this.reportFormat.abbr != 'HTML_EMBEDDED') {
                            var repData =  this.buildResult.reportData;
                            var repName =  this.buildResult.reportFileName;

                            this.buildResult.reportData = '';

                            var sliceSize = 1024;
                            var byteCharacters = atob(repData);
                            var bytesLength = byteCharacters.length;
                            var slicesCount = Math.ceil(bytesLength / sliceSize);
                            var byteArrays = new Array(slicesCount);

                            for (var sliceIndex = 0; sliceIndex < slicesCount; ++sliceIndex) {
                                var begin = sliceIndex * sliceSize;
                                var end = Math.min(begin + sliceSize, bytesLength);
                                var bytes = new Array(end - begin);
                                for (var offset = begin, i = 0 ; offset < end; ++i, ++offset) {
                                    bytes[i] = byteCharacters[offset].charCodeAt(0);
                                }
                                byteArrays[sliceIndex] = new Uint8Array(bytes);
                            }
                            var blob = new Blob(byteArrays, { type: "application/octet-stream" });

                            if (typeof window.navigator.msSaveBlob !== 'undefined') {
                                // IE workaround for "HTML7007: One or more blob URLs were revoked by closing the blob for which they were created. These URLs will no longer resolve as the data backing the URL has been freed."
                                window.navigator.msSaveBlob(blob, repName);
                            } else {
                                var URL = window.URL || window.webkitURL;
                                var downloadUrl = URL.createObjectURL(blob);
                                // use HTML5 a[download] attribute to specify filename
                                var a = document.createElement("a");
                                // safari doesn't support this yet
                                if (typeof a.download === 'undefined') {
                                  window.location = downloadUrl;
                                } else {
                                    a.href = downloadUrl;
                                    a.download = repName;
                                    document.body.appendChild(a);
                                    a.click();
                                }
                            }
                            setTimeout(function () { URL.revokeObjectURL(downloadUrl); }, 100); // cleanup
                        }
                    }
                    else {
                        this.buildResult.reportData = '';
                    }
                });
            });
        },
        loadReport() {
            this.$apiClient().then((client: any) => {
                client['report-api-controller'].getReportUsingGET(null, {
                    parameters: {
                        id: this.$route.params.id
                    }
                }).then((data: any) => {
                    if (data) {
                        this.report = Object.assign(this.report, data.body);
                        this.report.parameters.forEach(param => {
                            if(param.type==='BOOLEAN_UNCHECKED') {
                                param.value = false;
                            }
                            else if(param.type==='BOOLEAN_CHECKED') {
                                param.value = true;
                            }
                        });
                    }
                });
            });
        }
    },
    created: function() {
        this.loadReport();
    }
});
</script>
