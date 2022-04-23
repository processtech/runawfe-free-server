<template>
    <td :bgcolor="header.bcolor">
        <span v-if="header.format === 'DateTime'">
            <tr>
                <td>
                    <v-text-field
                        v-bind:value="start"
                        type="datetime-local"
                        dense
                        outlined
                        clearable
                        hide-details
                        @input="start = $event"
                        @blur="applyHeaderValue(dateTimeValue(), header.format, false)"
                        @keydown.enter="applyHeaderValue(dateTimeValue(), header.format, true)"
                    />
                </td>
            </tr>
            <tr/>
            <tr>
                <td>
                    <v-text-field
                        v-bind:value="end"
                        type="datetime-local"
                        dense
                        outlined
                        clearable
                        hide-details
                        @input="end = $event"
                        @blur="applyHeaderValue(dateTimeValue(), header.format, false)"
                        @keydown.enter="applyHeaderValue(dateTimeValue(), header.format, true)"
                    />
                </td>
            </tr>
        </span>
        <span v-else-if="header.format === 'Long'">
            <v-text-field ref="input"
                color="primary"
                v-bind:value="value"
                dense
                outlined
                clearable
                hide-details
                @blur="applyHeaderValue($event.target.value, header.format, false)"
                @keydown.enter="applyHeaderValue($event.target.value, header.format, true)"
            />
        </span>
        <span v-else>
            <v-text-field ref="input"
                color="primary"
                v-bind:value="value"
                dense
                outlined
                clearable
                hide-details
                label="Содержит"
                @blur="applyHeaderValue($event.target.value, header.format, false)"
                @keydown.enter="applyHeaderValue($event.target.value, header.format, true)"
            />
        </span>
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
            start: '',
            end: '',
            activeColor: 'blue',
        }
    },
    mounted: function () {
        if(this.header.format === 'DateTime') {
            if(this.value) {
                const [startDate, endDate] = this.value.split('|');
                if(startDate) {
                    this.start = this.getDateTimeFromValue(startDate);
                }
                if(endDate) {
                    this.end = this.getDateTimeFromValue(endDate);
                }
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
            const startDate = this.formatDate(this.start);
            const endDate = this.formatDate(this.end);
            if (startDate || endDate) {
                return [startDate, endDate].join('|');
            } else {
                return '';
            }
        },
        applyHeaderValue (val, format, reload: boolean) {
            let value = val;
            if (format === 'Long') {
                if (value) {
                    value = val.replace(/\D/g, '');
                }
            }
            if (!value) {
                if (format !== 'DateTime') {
                    this.$refs.input.reset();
                }
                value = null;
            }
            this.$emit('input', value);
            if(reload) {
                this.$emit('update-filter-and-reload-event');
            } else {
                this.$emit('update-filter-event');
            }
        }
    },
});
</script>