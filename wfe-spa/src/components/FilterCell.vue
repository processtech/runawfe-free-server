<template>
    <td ref="tdheader" bgcolor=''>
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
                        @blur="applyHeaderValue(dateTimeValue(), header.format)"
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
                        @blur="applyHeaderValue(dateTimeValue(), header.format)"
                    />
                </td>
            </tr>
        </span>
        <span v-else>
            <v-text-field ref="input"
                color="primary"
                v-bind:value="value"
                dense 
                outlined 
                clearable 
                hide-details
                @blur="applyHeaderValue($event.target.value, header.format)"
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
        }
    },
    mounted: function () {
    },
    methods: {
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
        applyHeaderValue (val, format) {
            let value = val;
            if (format === 'Long') {
                if (value) {
                    value = val.replace(/\D/g, '');
                }
            }
            if (value) {
                this.$refs.tdheader.setAttribute('bgcolor', '#FFFFE0');
            } else {
                this.$refs.tdheader.setAttribute('bgcolor', '');
                if (format !== 'DateTime') {
                    this.$refs.input.reset();
                }
            }
            this.$emit('input', value);
            this.$emit('update-filter-event');
        }
    },
});
</script>
