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
                        @click="getDefinitionsName"
            ></v-select>
            <v-select v-else-if="parameter.type === 'SWIMLANE'"
                        :items="swimlanes"
                        label="Роль"
                        v-model="parameter.value"
                        persistent-hint
                        single-line
                        @click="getSwimlanes"
            ></v-select>
            <v-text-field v-else-if="parameter.type === 'ACTOR_ID'"
                label="ID пользователя"
                :key="parameter.internalName"
                v-model="parameter.value"
                outlined
                dense
            ></v-text-field>
            <v-text-field v-else-if="parameter.type === 'ACTOR_NAME'"
                label="Имя пользователя"
                :key="parameter.internalName"
                v-model="parameter.value"
                outlined
                dense
            ></v-text-field>
            <v-text-field v-else-if="parameter.type === 'GROUP_ID'"
                label="ID группы"
                :key="parameter.internalName"
                v-model="parameter.value"
                outlined
                dense
            ></v-text-field>
            <v-text-field v-else-if="parameter.type === 'GROUP_NAME'"
                label="Имя группы"
                :key="parameter.internalName"
                v-model="parameter.value"
                outlined
                dense
            ></v-text-field>
            <v-text-field v-else-if="parameter.type === 'EXECUTOR_ID'"
                label="ID исполнителя"
                :key="parameter.internalName"
                v-model="parameter.value"
                outlined
                dense
            ></v-text-field>
            <v-text-field v-else-if="parameter.type==='EXECUTOR_NAME'"
                label="Имя исполнителя"
                :key="parameter.internalName"
                v-model="parameter.value"
                outlined
                dense
            ></v-text-field>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import Vue from 'vue';

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
            swimlanes:[]
        }
    },

    methods: {
        formatDate (date) {
            if (!date) return null
            const [year, month, day] = date.split('-')
            return `${day}.${month}.${year}`
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
                this.definitionNames.push('All BPs');
                this.definitions.data.forEach(definition => {
                    this.definitionNames.push(definition.name);
                });
            });
        },
        getSwimlanes () {
            this.getDefinitions().then(res => {
                this.swimlanes.push('All swimlanes');
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
        }
    }
});
</script>
