<template>
    <v-row>
        <v-col cols="2">
            {{ parameter.userName }}
            <span v-if="parameter.required" class="red--text"><strong> * </strong></span>
            <v-tooltip bottom v-if="parameter.description !== ''">
                <template v-slot:activator="{ on, attrs }">
                    <v-icon
                        color="primary"
                        dark
                        v-bind="attrs"
                        v-on="on"
                        small
                    >
                        mdi-information-outline
                    </v-icon>
                </template>
                <span>{{ parameter.description }}</span>
            </v-tooltip>
        </v-col>
        <v-col cols="2">
            <v-text-field v-if="parameter.type === 'STRING' || parameter.type === 'NUMBER'"
                :key="parameter.internalName"
                v-model="parameter.value"
                outlined
                dense
            ></v-text-field>
            <v-checkbox v-else-if="parameter.type === 'BOOLEAN_UNCHECKED'"
                :id="parameter.internalName"
                v-model="parameter.value"
            ></v-checkbox>
            <v-checkbox v-else-if="parameter.type === 'BOOLEAN_CHECKED'"
                :id="parameter.internalName"
                v-model="parameter.value"
            ></v-checkbox>
            <span v-else-if="parameter.type==='DATE'">
                <v-menu
                    :ref="parameter.internalName"
                    v-model="menu"
                    :close-on-content-click="false"
                    transition="scale-transition"
                    offset-y
                    max-width="290px"
                    min-width="auto"
                >
                    <template v-slot:activator="{ on, attrs }">
                        <v-text-field
                            v-model="parameter.value"
                            persistent-hint
                            prepend-icon="mdi-calendar"
                            readonly
                            v-bind="attrs"
                            v-on="on"
                        ></v-text-field>
                    </template>
                    <v-date-picker
                        v-model="buf"
                        no-title
                        @input="menu = false;parameter.value = formatDate(buf);"
                    ></v-date-picker>
                </v-menu>
            </span>
            <v-select v-else-if="parameter.type === 'PROCESS_NAME_OR_NULL'"
                        :items="definitionNames"
                        label="Имя процесса"
                        v-model="parameter.value"
                        persistent-hint
                        single-line
                        clearable
                        @click="getDefinitionsName"
            ></v-select>
            <v-select v-else-if="parameter.type === 'SWIMLANE'"
                        :items="swimlanes"
                        label="Роль"
                        v-model="parameter.value"
                        persistent-hint
                        single-line
                        clearable
                        @click="getSwimlanes"
            ></v-select>
            <v-select v-else-if="parameter.type === 'ACTOR_ID'"
                        :items="executors"
                        item-text="name"
                        item-value="id"
                        label="ID пользователя"
                        v-model="parameter.value"
                        persistent-hint
                        single-line
                        clearable
                        @click="getActors"
            >
                <template v-slot:selection="{ item }">
                    {{ getExecutorText(item) }}
                </template>
                <template v-slot:item="{ item }">
                    {{ getExecutorText(item) }}
                </template>
            </v-select>
            <v-select v-else-if="parameter.type === 'ACTOR_NAME'"
                        :items="executors"
                        item-text="name"
                        item-value="name"
                        label="Имя пользователя"
                        v-model="parameter.value"
                        persistent-hint
                        single-line
                        clearable
                        @click="getActors"
            >
                <template v-slot:selection="{ item }">
                    {{ getExecutorText(item) }}
                </template>
                <template v-slot:item="{ item }">
                    {{ getExecutorText(item) }}
                </template>
            </v-select>
            <v-select v-else-if="parameter.type === 'GROUP_ID'"
                        :items="executors"
                        item-text="name"
                        item-value="id"
                        label="ID группы"
                        v-model="parameter.value"
                        persistent-hint
                        single-line
                        clearable
                        @click="getGroups"
            ></v-select>
            <v-select v-else-if="parameter.type === 'GROUP_NAME'"
                        :items="executors"
                        item-text="name"
                        item-value="name"
                        label="Имя группы"
                        v-model="parameter.value"
                        persistent-hint
                        single-line
                        clearable
                        @click="getGroups"
            ></v-select>
            <v-select v-else-if="parameter.type === 'EXECUTOR_ID'"
                        :items="executors"
                        item-text="name"
                        item-value="id"
                        label="ID исполнителя"
                        v-model="parameter.value"
                        persistent-hint
                        single-line
                        clearable
                        @click="getExecutors()"
            >
                <template v-slot:selection="{ item }">
                    {{ getExecutorText(item) }}
                </template>
                <template v-slot:item="{ item }">
                    {{ getExecutorText(item) }}
                </template>
            </v-select>
            <v-select v-else-if="parameter.type === 'EXECUTOR_NAME'"
                        :items="executors"
                        item-text="name"
                        item-value="name"
                        label="Имя исполнителя"
                        v-model="parameter.value"
                        persistent-hint
                        single-line
                        clearable
                        @click="getExecutors()"
            >
                <template v-slot:selection="{ item }">
                    {{ getExecutorText(item) }}
                </template>
                <template v-slot:item="{ item }">
                    {{ getExecutorText(item) }}
                </template>
            </v-select>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import Vue from 'vue';
import { ExecutorDto } from '../ts/ExecutorDto';

export default Vue.extend({
    props:{
        parameter:{
            type:Object,
            required:true
        }
    },

    data: function () {
        return {
            menu: false,
            buf: '',
            definitions: [],
            definitionNames: [],
            swimlanes:[],
            executors: new Array<ExecutorDto>(),
            executorFilter: {
                name: null,
                fullName: null,
                description: null,
                type: null
            },
        }
    },

    methods: {
        formatDate (date) {
            if (!date) return null
            const [year, month, day] = date.split('-')
            return `${day}.${month}.${year}`
        },
        getExecutorText (item) {
            if (item.fullName) {
                return `${item.name} (${item.fullName})`
            } else {
                return `${item.name}`
            }
        },
        getDefinitions () {
            const query = {
                filters: {},
                pageNumber: '',
                pageSize: '',
                sortings: [],
                variables: []
            };
            return new Promise((resolve, reject) => {
                this.$apiClient().then((client: any) => {
                    client['process-definition-api-controller'].getDefinitionsUsingPOST(null, { requestBody: query }).then((data: any) => {
                        if (data.body) {
                            this.definitions = data.body;
                        }
                    }).then(res => resolve(res))
                      .catch(err => reject(err));
                });
            })
        },
        getDefinitionsName () {
            this.getDefinitions().then(res => {
                this.definitions.data.forEach(definition => {
                    this.definitionNames.push(definition.name);
                });
            });
        },
        getSwimlanes () {
            this.getDefinitions().then(res => {
                this.definitions.data.forEach(definition => {
                    this.$apiClient().then((client: any) => {
                        client['process-definition-api-controller'].getSwimlanesUsingGET_1(null, {
                            parameters: {
                                id: definition.versionId
                            }
                        }).then((data: any) => {
                            if (data) {
                                data.body.data.forEach(swimlane => {
                                    this.swimlanes.push(swimlane.scriptingName)
                                });
                            }
                        });
                    });
                });
            });
        },
        getExecutors (type) {
            this.executorFilter.type = type;
            const query = {
                filters: this.executorFilter,
                pageNumber: '',
                pageSize: '',
                sortings: [],
                variables: []
            };
            this.$apiClient().then((client: any) => {
                client['executor-api-controller'].getExecutorsUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.executors = body.data;
                    }
                });
            });
        },
        getActors () {
            this.getExecutors('N');
        },
        getGroups () {
            this.getExecutors('Y');
        }
    }
});
</script>
