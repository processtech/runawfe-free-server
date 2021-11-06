<template>
    <v-container
        id="my-tasks-view"
        fluid
        tag="section"
    >
        <v-data-table
            class="elevation-1 wfe-task-table"
            :item-class="getClass"
            :headers="headers"
            :items="tasks"
            item-key="id"
            :options.sync="options"
            :loading="loading"
            :footer-props="{
                disablePagination: false,
                disableItemsPerPage: false,
                itemsPerPageAllText: 'Все',
                itemsPerPageText: 'Строк на странице',
            }"
            >
            <template v-slot:[`item.createDate`]="{ item }">
                {{ new Date(item.createDate).toLocaleString() }}
            </template>
            <template v-slot:[`item.deadlineDate`]="{ item }">
                {{ new Date(item.deadlineDate).toLocaleString() }}
            </template>
            <template v-slot:[`footer.page-text`]="items">
                {{ items.pageStart }} - {{ items.pageStop }} из {{ items.itemsLength }}
            </template>
            <template v-slot:no-data>
                Данные отсутствуют
            </template>
            <template v-slot:[`body.prepend`]>
                <tr v-if="filter.visible" class="filter-row">
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
                <card-link :routeName="`Карточка задачи`" :id="item.id" :text="item.name" />
            </template>
            <template v-slot:[`item.definitionName`]="{ item }">
                <card-link :routeName="`Карточка процесса`" :id="item.processId" :text="item.definitionName" />
            </template>
            <template v-slot:top>
                <v-toolbar flat>
                    <v-spacer/>
                    <v-btn 
                        text 
                        icon 
                        @click="filter.visible = !filter.visible" 
                        v-model="filter.visible" 
                        color="rgba(0, 0, 0, 0.67)"
                    >
                        <v-icon>mdi-filter</v-icon>
                    </v-btn>
                    <columns-visibility :initialHeaders="initialHeaders" />
                    <color-description :colors="colors" />
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
    name: "TaskList",
    data() {
        return {
            filter: {
                visible: false,
                name: '',
                description: '',
                processId: '',
                definitionName: '',
                createDate: '',
                deadlineDate: '',
            },
            tasks: [],
            loading: true,
            options: new Options(),
            colors: [
                {
                    value: 'task1',
                    desc: 'Установленный срок задачи подходит к концу'
                },
                {
                    value: 'task2',
                    desc: 'Задача не выполнена в установленный срок'
                },
                {
                    value: 'task3',
                    desc: 'Задача получена по эскалации'
                },
                {
                    value: 'task4',
                    desc: 'Задача получена по замещению'
                }
            ],
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
                    value: 'description',
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
                    text: 'Процесс', 
                    value: 'definitionName',
                    visible: true,
                    width: '20em',
                },
                { 
                    text: 'Создана', 
                    value: 'createDate',
                    visible: true,
                    width: '12em',
                },
                { 
                    text: 'Время окончания', 
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
                } else if (h.value === 'definitionName') {
                    h.filter = (value: string): boolean => {
                        if (!this.filter.definitionName) return true;
                        return value.toLowerCase().indexOf(this.filter.definitionName.toLowerCase()) !== -1;
                    };
                } else if (h.value === 'createDate') {

                } else if (h.value === 'deadlineDate') {

                }
            });
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
    },
    methods: {
        getClass (task: any) {
            let cl = '';
            const timestamp = new Date().getTime();
            if (task.acquiredBySubstitution) {
                cl = 'task4';
            } else if (task.escalated) {
                cl = 'task3';
            } else if (task.deadlineDate != null && task.deadlineDate < timestamp) {
                cl = 'task2';
            } else if (task.deadlineWarningDate != null && task.deadlineWarningDate < timestamp) {
                cl = 'task1';
            }
            return cl;
        },
        getDataFromApi () {
            this.loading = true;
            const { page, itemsPerPage, sortBy, sortDesc } = this.options;
            const query = {
                filters: {},
                pageNumber: page,
                pageSize: itemsPerPage,
                sortings: Sorting.convert(sortBy, sortDesc),
                variables: Array
            };
            this.$apiClient().then((client: any) => {
                client['task-api-controller'].getTasksUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.tasks = body.data;
                    }
                    this.loading = false;
                });
            });
        },
    }
});
</script>
