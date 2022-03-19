<template>
    <span>
        <tr>
            <td>
                <v-text-field
                    v-model="start"
                    type="datetime-local"
                    dense
                    outlined
                    clearable
                    hide-details
                    @blur = "apply"
                />
            </td>
        </tr>
        <tr/>
        <tr>
            <td>
                <v-text-field
                    v-model="end"
                    type="datetime-local"
                    dense
                    outlined
                    clearable
                    hide-details
                    @blur = "apply"
                />
            </td>
        </tr>
    </span>
</template>

<script lang="ts">
import Vue from 'vue';
import { PropOptions } from 'vue';

export default Vue.extend({
    name: "DateTimeFilterCell",
    data() {
        return {
            start: '',
            end: '',
            filter: ''
        }
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
        apply () {
            const startDate = this.formatDate(this.start);
            const endDate = this.formatDate(this.end);
            if (startDate || endDate) {
                this.filter = [startDate, endDate].join('|');
            } else {
                this.filter = '';
            }
            this.$emit('update-filter-event', this.filter);
        }
    },
});
</script>
