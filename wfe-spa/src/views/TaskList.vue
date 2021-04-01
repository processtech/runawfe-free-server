<template>
    <v-container
        id="regular-tables-view"
        fluid
        tag="section"
    >
        <v-data-table
            class="elevation-1 wfe-task-table"
            :headers="headers"
            :items="tasks"
            item-key="id"
            :options.sync="options"
            :loading="loading"
            :search="filter.search"
            :footer-props="{
                disablePagination: false,
                disableItemsPerPage: false,
                itemsPerPageAllText: 'Все',
                itemsPerPageText: 'Строк на странице',
            }"
            >
            <template v-slot:[`item.creationDate`]="{ item }">
                {{ new Date(item.creationDate).toLocaleString() }}
            </template>
            <template v-slot:[`item.deadlineDate`]="{ item }">
                {{ new Date(item.deadlineDate).toLocaleString() }}
            </template>
            <template v-slot:[`footer.page-text`]="items">
                {{ items.pageStart }} - {{ items.pageStop }} из {{ items.itemsLength }}
            </template>
            <template v-slot:[`body.prepend`]>
                <tr v-if="filter.visible">
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
                <card-link v-on:get-id="saveTaskId" :routeName="`Карточка задачи`" :id="item.id" :text="item.name" />
            </template>
            <template v-slot:[`item.definitionName`]="{ item }">
                <card-link :routeName="`Карточка процесса`" :id="item.id" :text="item.definitionName" />
            </template>
            <template v-slot:top>
                <v-toolbar flat>
                    <v-spacer/>
                    <v-btn 
                        text 
                        icon 
                        @click="filter.visible = !filter.visible" 
                        v-model="filter.visible" 
                        color="grey"
                    >
                        <v-icon >mdi-filter</v-icon>
                    </v-btn>
                    <v-dialog v-model="dialog" max-width="500px">
                        <template v-slot:activator="{ on, attrs }">
                            <v-btn
                                text 
                                icon
                                v-bind="attrs"
                                v-on="on"
                                color="grey"
                            >
                                <v-icon>mdi-view-grid-plus</v-icon>
                            </v-btn>
                        </template>
                        <v-card>
                            <v-card-title>
                                <span class="headline">Настройка вида</span>
                            </v-card-title>
                            <v-card-text>
                                <v-container>
                                    <v-row>
                                        <v-col v-for="header in initialHeaders" :key="header.value" cols="12" sm="6" md="4"> 
                                            <v-checkbox 
                                                v-model="header.visible" 
                                                :label="header.text"
                                                @change="initialHeaders" 
                                            />
                                        </v-col>
                                    </v-row>
                                </v-container>
                            </v-card-text>
                        </v-card>
                    </v-dialog>
                </v-toolbar>
            </template>
        </v-data-table>

    </v-container>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';
import { Options, Sorting } from '../ts/options';

export default Vue.extend({
    name: "TaskList",

    data() {
        return {
            dialog: false,
            filter: {
                visible: true,
                search: '',
                name: '',
                description: '',
                processId: '',
                category: '',
                definitionName: '',
                creationDate: '',
                deadlineDate: '',
            },
            tasks: [],
            loading: true,
            options: new Options(),
            initialHeaders: [
                {
                    text: 'Задача',
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
                    text: '№ экз.', 
                    value: 'processId',
                    visible: true,
                    width: '7em',
                },
                { 
                    text: 'Тип процесса', 
                    value: 'category',
                    visible: false,
                    sortable: false,
                    width: '20em',
                },
                { 
                    text: 'Процесс', 
                    value: 'definitionName',
                    visible: true,
                    width: '20em',
                },
                { 
                    text: 'Создана', 
                    value: 'creationDate',
                    visible: true,
                    width: '12em',
                },
                { 
                    text: 'Выполнена', 
                    value: 'deadlineDate',
                    visible: true,
                    width: '12em',
                },
            ]
        }
    },
    computed: {
        headers(): any {
            this.initialHeaders.forEach((h: any) => {
                if (h.value === 'name') {
                    h.filter = (value: string): boolean => {
                        if (!this.filter.name) return true;
                        return value.toLowerCase().indexOf(this.filter.name.toLowerCase()) !== -1;
                    };
                } else if (h.value === 'description') {
                    h.filter = (value: string): boolean => {
                        if (!this.filter.description) return true;
                        return value.toLowerCase().indexOf(this.filter.description.toLowerCase()) !== -1;
                    };
                } else if (h.value === 'processId') {
                    h.filter = (value: number): boolean => {
                        if (!this.filter.processId) return true;
                        return value == parseInt(this.filter.processId);
                    };
                } else if (h.value === 'category') {
                    h.filter = (value: string): boolean => {
                        if (!this.filter.category) return true;
                        return value.toLowerCase().indexOf(this.filter.category.toLowerCase()) !== -1;
                    };
                } else if (h.value === 'definitionName') {
                    h.filter = (value: string): boolean => {
                        if (!this.filter.definitionName) return true;
                        return value.toLowerCase().indexOf(this.filter.definitionName.toLowerCase()) !== -1;
                    };
                } else if (h.value === 'creationDate') {

                } else if (h.value === 'deadlineDate') {

                }
            });
            return this.initialHeaders.filter((h: any) => {
                return h.visible;
            });
        },
        taskId: sync('app/task@id'),
    },
    watch: {
        options: {
            handler () {
                this.getDataFromApi()
            },
            deep: true,
        },
    },
    methods: {
        saveTaskId (id: any) {
            this.taskId = id;
        },
        getDataFromApi () {
            this.loading = true;
            const { page, itemsPerPage, sortBy, sortDesc } = this.options;
            const query = {
                filters: {},
                pageNumber: page,
                pageSize: itemsPerPage,
                sortings: Sorting.convert(sortBy, sortDesc),
                variables: []
            };
            this.$apiClient().then((client: any) => {
                client.apis['task-api-controller'].getTasksUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.tasks = body.tasks;
                    }
                    this.loading = false;
                });
            }); 
        },
    }
});
</script>