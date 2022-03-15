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
            :server-items-length="total"
            :loading="loading"
            :footer-props="{
                disablePagination: false,
                disableItemsPerPage: false,
                itemsPerPageAllText: 'Все',
                itemsPerPageText: 'Строк на странице',
            }"
            >
            <template v-for="header in headers" v-slot:[`item.${header.value}`]="{ item }">
                <div v-if="header.dynamic">
                    {{ getVariableValue(header.text, item) }}
                </div>
                <div v-else-if="header.value==='createDate' || header.value==='deadlineDate'">
                    {{ new Date(item[header.value]).toLocaleString() }}
                </div>
                <div v-else-if="header.value==='name'">
                    <card-link :routeName="`Карточка задачи`" :id="item.id" :text="item.name" />
                </div>
                <div v-else-if="header.value==='definitionName'">
                    <card-link :routeName="`Карточка процесса`" :id="item.processId" :text="item.definitionName" />
                </div>
                <div v-else>
                    {{ item[header.value] }}
                </div>
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
                        color="rgba(0, 0, 0, 0.67)"
                         @click="getDataFromApi()"
                    >
                        <v-icon>mdi-reload</v-icon>
                    </v-btn>
                    <v-btn 
                        text 
                        icon 
                        @click="filterVisible = !filterVisible" 
                        v-model="filterVisible" 
                        color="rgba(0, 0, 0, 0.67)"
                    >
                        <v-icon>mdi-filter</v-icon>
                    </v-btn>
                    <columns-visibility :initialHeaders="initialHeaders" :variables="variables" :filter="filter" @update-data-event="updateData"/>
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
            filterVisible: false,
            filter: {
                name: null,
                description: null,
                processId: null,
                definitionName: null,
                createDate: null,
                deadlineDate: null
            },
            variables: [],
            total: 0,
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
    mounted() {
        if (localStorage.getItem('runawfe@task-list-variables')) {
            try {
                this.variables = JSON.parse(localStorage.getItem('runawfe@task-list-variables'));
            } catch(e) {
                localStorage.removeItem('runawfe@task-list-variables');
            }
        }
        if (localStorage.getItem('runawfe@task-list-initialHeaders')) {
            try {
                this.initialHeaders = JSON.parse(localStorage.getItem('runawfe@task-list-initialHeaders'));
            } catch(e) {
                localStorage.removeItem('runawfe@task-list-initialHeaders');
            }
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
        updateData () {
            this.getDataFromApi();
            localStorage.setItem('runawfe@task-list-variables', JSON.stringify(this.variables));
            localStorage.setItem('runawfe@task-list-initialHeaders', JSON.stringify(this.initialHeaders));
        },
        getVariableValue(variableName, data) {
            for (let variable of data.variables) {
                if (variableName == variable.name ) {
                    const format = variable.format.replace('ru.runa.wfe.var.format.','');
                    if (format === 'DateFormat') {
                        return new Date(variable.value).toLocaleString();
                    } else {
                        return variable.value;
                    }
                }
            }
        },
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
                filters: this.filter,
                pageNumber: page,
                pageSize: itemsPerPage,
                sortings: Sorting.convert(sortBy, sortDesc),
                variables: this.variables
            };
            this.$apiClient().then((client: any) => {
                client['task-api-controller'].getTasksUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.tasks = body.data;
                        this.total = body.total;
                    }
                    this.loading = false;
                }).catch((error: any) => {
                    this.loading = false;
                    this.tasks = [];
                    this.total = 0;
                });;
            });
        },
    }
});
</script>
