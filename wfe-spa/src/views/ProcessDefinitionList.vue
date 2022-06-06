<template>
    <v-container
        id="processes-definition-view"
        fluid
        tag="section"
    >
        <v-data-table
            class="elevation-1 wfe-process-table"
            :headers="headers"
            :items="definitions"
            item-key="id"
            :options.sync="options"
            :server-items-length="total"
            :loading="loading"
            :footer-props="{
                disablePagination: false,
                disableItemsPerPage: false,
                itemsPerPageAllText: 'Все',
                itemsPerPageText: 'Строк на странице',
            }"
            >
            <template v-slot:[`item.createActor`]="{ item }">
                {{ item.createActor ? item.createActor.name : '' }}
            </template>
            <template v-slot:[`item.updateActor`]="{ item }">
                {{ item.updateActor ? item.updateActor.name : '' }}
            </template>
            <template v-slot:[`item.createDate`]="{ item }">
                {{ new Date(item.createDate).toLocaleString() }}
            </template>
            <template v-slot:[`item.updateDate`]="{ item }">
                {{ item.updateDate ? new Date(item.updateDate).toLocaleString() : '' }}
            </template>
            <template v-slot:[`item.start`]="{ item }">
                <v-icon
                    color="green"
                    class="mr-2"
                    :disabled="!item.canBeStarted"
                    @click="openStartForm(item)" 
                >
                    mdi-play-circle
                </v-icon>
            </template>
            <template v-slot:[`footer.page-text`]="items">
                {{ items.pageStart }} - {{ items.pageStop }} из {{ items.itemsLength }}
            </template>
            <template v-slot:no-data>
                Данные отсутствуют
            </template>
            <template v-slot:[`body.prepend`]>
                <tr v-if="filterVisible" class="filter-row">
                    <td v-for="header in headers" :key="header.value">
                        <v-text-field 
                            v-if="header.value != 'start'"
                            color="primary"
                            v-model="filter[header.value]" 
                            dense 
                            outlined 
                            clearable 
                            hide-details
                        />
                    </td>
                </tr>
            </template>
            <template v-slot:top>
                <v-toolbar flat>
                    <v-spacer/>
                    <v-btn 
                        text 
                        icon 
                        @click="filterVisible = !filterVisible" 
                        v-model="filterVisible" 
                        color="rgba(0, 0, 0, 0.67)"
                    >
                        <v-icon>mdi-filter</v-icon>
                    </v-btn>
                    <columns-visibility :initialHeaders="initialHeaders" />
                </v-toolbar>
            </template>
        </v-data-table>
    </v-container>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';
import { Options, Sorting } from '../ts/Options';

export default Vue.extend({
    name: "ProcessDefinitionList",
    
    data() {
        return {
            timeout: 10000,
            dialog: false,
            filterVisible: false,
            filter: {
                name: null,
                description: null,
                createDate: null,
                createActor: null,
                updateDate: null,
                updateActor: null,
            },
            total: 0,
            definitions: [],
            loading: true,
            options: new Options(),
            initialHeaders: [
                {   
                    text: 'Запустить', 
                    value: 'start',
                    align: 'center',
                    visible: true,
                    sortable: false,
                    width: '1px'
                },
                {
                    text: 'Имя',
                    align: 'start', 
                    value: 'name',
                    visible: true,
                    width: '20em',
                },
                { 
                    text: 'Описание', 
                    value:'description',
                    visible: false,
                    width: '20em',
                },
                { 
                    text: 'Дата загрузки', 
                    value: 'createDate',
                    visible: true,
                    width: '12em',
                },
                { 
                    text: 'Автор загрузки', 
                    value: 'createActor',
                    visible: true,
                    sortable: false,
                    width: '12em',
                },
                { 
                    text: 'Дата обновления', 
                    value: 'updateDate',
                    visible: false,
                    width: '12em',
                },
                { 
                    text: 'Автор обновления', 
                    value: 'updateActor',
                    visible: false,
                    sortable: false,
                    width: '12em',
                }
            ]
        }
    },
    computed: {
        headers(): any {
            return this.initialHeaders.filter((h: any) => {
                return h.visible;
            });
        },
    },
    watch: {
        options: {
            handler () {
                this.getDataFromApi()
            },
            deep: true,
        },
        filter: {
            handler () {
                this.getDataFromApi()
            },
            deep: true,
        }
    },
    methods: {
        openStartForm (item) {
            this.$router.push({ name: "ProcessDefinitionCard", params: { versionId: item.versionId.toString() } });
        },
        getDataFromApi () {
            this.loading = true;
            const { page, itemsPerPage, sortBy, sortDesc } = this.options;
            const query = {
                filters: this.filter,
                pageNumber: page,
                pageSize: itemsPerPage,
                sortings: Sorting.convert(sortBy, sortDesc),
                variables: []
            };
            this.$apiClient().then((client: any) => {
                client['process-definition-api-controller'].getProcessDefinitionsUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.definitions = body.data;
                        this.total = body.total;
                    }
                    this.loading = false;
                });
            });
        },
    },
});
</script>
