import { WfeReportParameter } from './WfeReportParameter';

export class WfeReport {
    id: number|null = null;
    name: string = '';
    description: string = '';
    category: string = '';
    parameters: Array<WfeReportParameter>|null = null;

    getInfo(): object {
        return {
            'Название': this.name,
            'Описание': this.description
        };
    }
}