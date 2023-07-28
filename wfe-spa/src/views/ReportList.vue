<template>
    <v-container
        id="reports-view"
        fluid
        tag="section"
    >
        <v-data-table
            class="elevation-1 wfe-report-table"
            :headers="headers"
            :items="reports"
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
            <template v-slot:[`item.name`]="{ item }">
                <card-link :routeName="`Карточка отчета`" :id="item.id" :text="item.name" />
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
    name: "ReportList",
    
    data() {
        return {
            timeout: 10000,
            dialog: false,
            filterVisible: false,
            filter: {
                name: null,
                description: null,
                type: null,
            },
            total: 0,
            reports: [],
            loading: true,
            options: new Options(),
            initialHeaders: [
                {
                    text: 'Название',
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
                    text: 'Тип', 
                    value: 'category',
                    visible: true,
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
                client['report-controller'].getReportsUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.reports = body.data;
                        this.total = body.total;
                    }
                    this.loading = false;
                });
            });
        },
    },
});
</script>
