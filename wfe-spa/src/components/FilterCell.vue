<template>
    <td :bgcolor="header.bcolor" v-if="header.filterable">
        <span v-if="header.format === 'DateTime'">
            <tr>
                <td>
                    <v-text-field
                        v-model="startDate"
                        type="datetime-local"
                        dense
                        outlined
                        clearable
                        hide-details
                        @blur="applyHeaderValue(dateTimeValue(), false)"
                        @keydown.enter="applyHeaderValue(dateTimeValue(), true)"
                    />
                </td>
            </tr>
            <tr>
                <td>
                    <v-text-field
                        v-model="endDate"
                        type="datetime-local"
                        dense
                        outlined
                        clearable
                        hide-details
                        @blur="applyHeaderValue(dateTimeValue(), false)"
                        @keydown.enter="applyHeaderValue(dateTimeValue(), true)"
                    />
                </td>
            </tr>
        </span>
        <span v-else-if="header.format === 'Long'">
            <tr>
                <td>
                    <v-text-field
                        color="primary"
                        label="от"
                        v-model="startNumber"
                        dense
                        outlined
                        clearable
                        hide-details
                        @blur="applyHeaderValue(numberRangeValue(), false)"
                        @keydown.enter="applyHeaderValue(numberRangeValue(), true)"
                    />
                </td>
            </tr>
            <tr>
                <td>
                    <v-text-field
                        color="primary"
                        label="до"
                        v-model="endNumber"
                        dense
                        outlined
                        clearable
                        hide-details
                        @blur="applyHeaderValue(numberRangeValue(), false)"
                        @keydown.enter="applyHeaderValue(numberRangeValue(), true)"
                    />
                </td>
            </tr>
        </span>
        <span v-else-if="header.format === 'String' && header.selectOptions">
            <v-select
                v-model="selectedValue"
                :items="header.selectOptions"
                dense
                clearable
                @blur="applyHeaderValue(selectedValue, false)"
                @keydown.enter="applyHeaderValue(selectedValue, true)"
            />
        </span>
        <span v-else>
            <v-text-field
                color="primary"
                v-bind:value="value"
                dense
                outlined
                clearable
                hide-details
                label="Содержит"
                @blur="applyHeaderValue($event.target.value, false)"
                @keydown.enter="applyHeaderValue($event.target.value, true)"
            />
        </span>
    </td>
    <td v-else>
    </td>
</template>

<script lang="ts">
import Vue from 'vue';
import { PropOptions } from 'vue';

export default Vue.extend({
    name: "FilterCell",
    props: {
        value: String,
        header: {
            type: Object
        },
    },
    data() {
        return {
            startDate: '',
            endDate: '',
            startNumber: '',
            endNumber: '',
            selectedValue: '',
        }
    },
    mounted: function () {
        if(this.header.format === 'DateTime') {
            if(this.value) {
                const [startDate, endDate] = this.value.split('|');
                if(startDate) {
                    this.startDate = this.getDateTimeFromValue(startDate);
                }
                if(endDate) {
                    this.endDate = this.getDateTimeFromValue(endDate);
                }
            }
        } else if(this.header.format === 'Long') {
            if(this.value) {
                const [startNumber, endNumber] = this.value.split('-');
                if(startNumber) {
                    this.startNumber = startNumber;
                }
                if(endNumber) {
                    this.endNumber = endNumber;
                }
            }
        } else if(this.header.format === 'String' && this.header.selectOptions) {
            if(this.value) {
                this.selectedValue = this.value;
            }
        }
    },
    methods: {
        getDateTimeFromValue (val) {
            //local date to yyyy-MM-ddThh:mm
            if (val) {
                const [date, time] =  val.split(' ');
                const [day, month, year] = date.split('.');
                return `${year}-${month}-${day}T${time}`
            }
            return '';
        },
        formatDate (val) {
            //val always: yyyy-MM-ddThh:mm
            if (val) {
                const [date, time] = val.split('T');
                const [year, month, day] = date.split('-');
                return `${day}.${month}.${year} ${time}`;
            }
            return '';
        },
        dateTimeValue () {
            const startDate = this.formatDate(this.startDate);
            const endDate = this.formatDate(this.endDate);
            if (startDate || endDate) {
                return [startDate, endDate].join('|');
            } else {
                return '';
            }
        },
        numberRangeValue () {
            if(this.startNumber) {
                this.startNumber = this.startNumber.replace(/\D/g, '');
            }
            if(this.endNumber) {
                this.endNumber = this.endNumber.replace(/\D/g, '');
            }
            if (this.startNumber || this.endNumber) {
                return [this.startNumber, this.endNumber].join('-');
            } else {
                return '';
            }
        },
        applyHeaderValue (val, reload: boolean) {
            this.$emit('input', val);
            if(reload) {
                this.$emit('update-filter-and-reload-event');
            } else {
                this.$emit('update-filter-event');
            }
        }
    },
});
</script>