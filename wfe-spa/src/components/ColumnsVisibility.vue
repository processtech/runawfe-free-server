<template>
    <v-dialog v-model="dialog" max-width="500px">
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
                    @click="dialog = false; showVarField = false"
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
                        <v-col cols="12" class="d-flex justify-end">
                            <v-text-field
                                outlined
                                dense
                                required
                                clearable
                                v-model="variableName"
                                v-if="showVarField"
                                label="Введите имя переменной и нажмите Enter"
                                @keydown.enter="addVariableToHeader"
                            ></v-text-field>
                        </v-col>
                    </v-row>
                    <v-row>
                        <v-col v-for="header in initialHeaders" :key="header.value" cols="12" sm="6" md="4"> 
                            <v-checkbox 
                                dense
                                class="mt-0"
                                color="success"
                                hide-details
                                v-model="header.visible" 
                                :label="header.text"
                            />
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
        initialHeaders: {
            type: Array
        } as PropOptions<Header[]>,
    },
    data() {
        return {
            dialog: false,
            showVarField: false,
            variableName: '',
        }
    },
    methods: {
        selectAll () {
            for (let header of this.initialHeaders) {
                header.visible = true;
            }
        },
        toggleVarField () {
            this.showVarField = !this.showVarField; 
            this.variableName = '';
        },
        addVariableToHeader () {
            if (this.variableName) {
                let header = new Header();
                header.text = this.variableName;
                header.align = '';
                header.value = 'var' + this.initialHeaders.length + 1;
                header.visible = true;
                header.width = '10em';
                header.sortable = true;
                this.initialHeaders.push(header);
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
