<template>
    <v-card>
        <v-tabs
            v-model="tab"
            align-with-title
            background-color="transparent"
        >
            <v-tab
                v-for="item in items"
                :key="item.header"
            >
                {{ item.header }}
            </v-tab>

            <v-tabs-items v-model="tab">
                <v-tab-item
                    v-for="item in items"
                    :key="item.header"
                >
                    <v-container fluid>
                        <v-row>
                            <v-col
                                v-for="chart in item.charts"
                                :key="chart.title"
                                cols="12"
                                md="4"
                            >
                                <v-card
                                    class="mt-4 mx-auto"
                                >
                                    <v-sheet
                                        class="v-sheet--offset mx-auto"
                                        color="green"
                                        elevation="12"
                                        max-width="calc(100% - 32px)"
                                    >
                                        <v-sparkline
                                            type="trend"
                                            :labels="chart.labels"
                                            :value="chart.values"
                                            color="white"
                                            line-width="2"
                                            padding="30"
                                            
                                        ></v-sparkline>
                                    </v-sheet>

                                    <v-card-text class="pt-0">
                                        <div class="title font-weight-light mb-2">
                                            {{chart.title}}
                                        </div>
                                    </v-card-text>
                                </v-card>
                            </v-col>
                        </v-row>
                    </v-container>
                </v-tab-item>
            </v-tabs-items>
        </v-tabs>
    </v-card>
</template>

<script lang="ts">
import Vue from 'vue';
import SwaggerClient from 'swagger-client';
import { get, sync } from 'vuex-pathify';

export default Vue.extend({
    name: "Desktop",

    data() {
      return {
            tab: null,
            items: [
                {
                    header: 'Задачи',
                    charts: [
                        {
                            title: 'Мои задачи',
                            labels: [
                                '01.01.2021',
                                '11.02.2021',
                                '08.03.2021',
                                '01.05.2021',
                            ],
                            values: [
                                5,
                                25,
                                15,
                                45
                            ]
                        },
                        {
                            title: 'Задачи по сотрудникам',
                            labels: [
                                '01.01.2021',
                                '11.02.2021',
                                '08.03.2021',
                                '01.05.2021',
                            ],
                            values: [
                                7,
                                75,
                                15,
                                25
                            ]
                        },
                        {
                            title: 'Задачи по подразделениям',
                            labels: [
                                '01.01.2021',
                                '11.02.2021',
                                '08.03.2021',
                                '01.05.2021',
                            ],
                            values: [
                                95,
                                25,
                                35,
                                75
                            ]
                        },
                    ]
                },
                {
                    header: 'Бизнес процессы',
                    charts: [
                        {
                            title: 'Всего',
                            labels: [
                                '01.01.2021',
                                '11.02.2021',
                                '08.03.2021',
                                '01.05.2021',
                            ],
                            values: [
                                7,
                                75,
                                15,
                                25
                            ]
                        },
                        {
                            title: 'По названию',
                            labels: [
                                '01.01.2021',
                                '11.02.2021',
                                '08.03.2021',
                                '01.05.2021',
                            ],
                            values: [
                                5,
                                25,
                                15,
                                45
                            ]
                        },
                        {
                            title: 'По типам',
                            labels: [
                                '01.01.2021',
                                '11.02.2021',
                                '08.03.2021',
                                '01.05.2021',
                            ],
                            values: [
                                95,
                                25,
                                35,
                                75
                            ]
                        },
                    ]
                },
            ],
      }
    },
    watch: {
        
    },
    methods: {
        
    },
});
</script>

<style lang="sass">
.v-sheet--offset
  top: -24px
  position: relative
</style>