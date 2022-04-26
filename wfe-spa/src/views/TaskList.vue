<template>
    <v-container
        id="my-tasks-view"
        fluid
        tag="section"
    >
        <wfe-tables
            :initialHeaders="initialHeaders"
            :records="tasks"
            :colors="colors"
            :total="total"
            :loading="loading"
            :routeName="`Карточка задачи`"
            :prefixLocalStorageName="`runawfe@task-list`"
            :dynamic="true"
            @get-data-event="onGetData"
        />
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
            total: 0,
            tasks: [],
            loading: true,
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
                    bcolor: '',
                    format: 'String',
                    link: true,
                    filterable: true,
                },
                {
                    text: 'Описание',
                    value: 'description',
                    visible: false,
                    width: '20em',
                    bcolor: '',
                    format: 'String',
                    filterable: true,
                },
                {
                    text: '№ экз.',
                    value: 'processId',
                    visible: true,
                    width: '7em',
                    bcolor: '',
                    format: 'Long',
                    filterable: true,
                },
                {
                    text: 'Процесс',
                    value: 'definitionName',
                    visible: true,
                    width: '20em',
                    bcolor: '',
                    format: 'String',
                    filterable: true,
                },
                {
                    text: 'Создана',
                    value: 'createDate',
                    visible: true,
                    width: '12em',
                    bcolor: '',
                    format: 'DateTime',
                    filterable: true,
                },
                {
                    text: 'Время окончания',
                    value: 'deadlineDate',
                    visible: true,
                    width: '12em',
                    bcolor: '',
                    format: 'DateTime',
                    filterable: true,
                },
            ]
        }
    },
    mounted() {
    },
    computed: {
    },
    watch: {
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
        getClasses (tasks: any) {
            tasks.forEach(task => {
                task.class = this.getClass(task);
            });
        },
        getFilters (filter) {
            let result = Object.assign({}, filter);
            for (let prop in filter) {
                if (filter.hasOwnProperty(prop)) {
                    if (filter[prop] !== null && filter[prop] !== '') {
                        let header = this.initialHeaders.find(h => h.value === prop);
                        if(!header || (header.format !== 'DateTime' && header.format !== 'Long' && !header.selectOptions)) {
                            result[prop] = '*' + filter[prop].trim() + '*/i';
                        }
                    }
                }
            }
            return result;
        },
        onGetData (options, filter, variables) {
            this.loading = true;
            const { page, itemsPerPage, sortBy, sortDesc } = options;
            const query = {
                filters: this.getFilters(filter),
                pageNumber: page,
                pageSize: itemsPerPage,
                sortings: Sorting.convert(sortBy, sortDesc),
                variables: variables
            };
            this.$apiClient().then((client: any) => {
                client['task-api-controller'].getTasksUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.tasks = body.data;
                        this.total = body.total;
                        this.getClasses(this.tasks);
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
