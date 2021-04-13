<template>
    <v-dialog v-model="dialog" max-width="800px">
        <template v-slot:activator="{ on, attrs }">
            <v-btn
                text 
                icon
                v-bind="attrs"
                v-on="on"
                color="rgba(0, 0, 0, 0.67)"
            >
                <v-icon>mdi-information-outline</v-icon>
            </v-btn>
        </template>
        <v-card>
            <v-card-title>
                <h2>Информация</h2>
                <v-spacer />
                <v-btn
                    icon
                    color="rgba(0, 0, 0, 0.67)"
                    @click="dialog = false"
                >
                    <v-icon>mdi-close</v-icon>
                </v-btn>
            </v-card-title>
            <v-card-text>
                <v-container>
                    <v-row v-for="color in colors" :key="color.value" class="mb-1">
                        <v-col cols="4" v-bind:class="getClass(color)" /> 
                        <v-col cols="8"> 
                            {{ color.desc }}    
                        </v-col>
                    </v-row>
                </v-container>
            </v-card-text>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue';

export default Vue.extend({
    name: "ColorDescription",
    props: {
        colors: Object
    },
    data() {
        return {
            dialog: false,
        }
    },
    methods: {
        getClass: function(color: any): any {
            return color.value;
        }
    }
});
</script>
