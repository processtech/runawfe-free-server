<template>
    <v-dialog v-model="dialog" max-width="500px" @click:outside="updateData" @keydown.esc="updateData">
        <template v-slot:activator="{ on, attrs }">
            <v-btn
                text
                icon
                v-bind="attrs"
                v-on="on"
                color="rgba(0, 0, 0, 0.67)"
            >
                <v-icon>mdi-view-grid-plus</v-icon>
            </v-btn>
        </template>
        <v-card>
            <v-card-title>
                <h2>Настройка вида</h2>
                <v-spacer />
                <v-btn
                    icon
                    color="rgba(0, 0, 0, 0.67)"
                    @click="dialog = false; showVarField = false; updateData()"
                >
                    <v-icon>mdi-close</v-icon>
                </v-btn>
            </v-card-title>
            <v-card-text>
                <v-divider />
                <v-container>
                    <v-row>
                        <v-col cols="12" class="d-flex justify-end">
                            <v-btn
                                class="d-inline-block"
                                text
                                v-if="variables"
                                @click = "toggleVarField"
                            >
                                Добавить переменную
                            </v-btn>
                            <v-btn
                                class="d-inline-block"
                                text
                                @click = "selectAll"
                            >
                                Выбрать всё
                            </v-btn>
                            <v-btn
                                class="d-inline-block"
                                text
                                @click = "unSelectAll"
                            >
                                Убрать всё
                            </v-btn>
                        </v-col>
                    </v-row>
                    <v-row>
                        <v-col cols="12">
                            <v-text-field
                                outlined
                                dense
                                required
                                clearable
                                v-model="variableName"
                                v-if="showVarField"
                                label="Введите имя переменной и нажмите Enter"
                                @keydown.enter="addVariable"
                            ></v-text-field>
                        </v-col>
                    </v-row>
                    <v-row>
                        <v-col v-for="header in initialHeaders" :key="header.value" cols="12" sm="6" md="4" class="d-flex">
                            <v-checkbox
                                dense
                                class="mt-0"
                                color="success"
                                hide-details
                                v-model="header.visible"
                                :label="header.text"
                                @change = "changeVisible($event, header.value)"
                            />
                            <v-btn  v-if="header.dynamic"
                                icon
                                x-small
                                color="red"
                                @click = "removeVariable(header.value)"
                            >
                                <v-icon>mdi-close</v-icon>
                            </v-btn>
                        </v-col>
                    </v-row>
                </v-container>
            </v-card-text>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue';
import { Header } from '../ts/Options';
import { PropOptions } from 'vue';

export default Vue.extend({
    name: "ColumnsVisibility",
    props: {
        variables: Array,
        initialHeaders: {
            type: Array
        } as PropOptions<Header[]>,
        filter: Object,
    },
    data() {
        return {
            dialog: false,
            showVarField: false,
            variableName: '',
        }
    },
    methods: {
        updateData () {
            this.$emit('update-data-event');
        },
        selectAll () {
            for (let header of this.initialHeaders) {
                header.visible = true;
            }
        },
        toggleVarField () {
            this.showVarField = !this.showVarField;
            this.variableName = '';
        },
        findVariableIndex (variableName) {
            return this.initialHeaders.findIndex(h => h.text === variableName);
        },
        changeVisible(check, name) {
            if (!check) {
                delete this.filter[name];
            }
        },
        addVariable () {
            if (this.variableName &&  this.findVariableIndex(this.variableName) === -1) {
                let header = new Header();
                const index = this.variables.length + 1;
                header.text = this.variableName;
                header.align = '';
                header.value = this.variableName;
                header.dynamic = true;
                header.visible = true;
                header.width = '10em';
                header.sortable = false;
                this.initialHeaders.push(header);
                this.variables.push(this.variableName);
            }
        },
        removeVariable (variableName) {
            if (variableName) {
                const headerIndx = this.findVariableIndex(variableName);
                if (headerIndx > 0){
                    this.initialHeaders.splice(headerIndx, 1);
                    const varIndx = this.variables.indexOf(variableName);
                    this.variables.splice(varIndx, 1);
                    delete this.filter[variableName];
                }
            }
        },
        unSelectAll () {
            for (let header of this.initialHeaders) {
                header.visible = false;
            }
        },
    },
});
</script>
