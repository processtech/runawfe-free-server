export class WfeProcess {
    id: number|null = null;
    definitionName: string = '';
    executionStatus: string = '';
    startDate: Date|null = null;
    endDate: Date|null = null; 

    getInfo(): object {
        return {
            'Номер': this.id,
            'Имя процесса': this.definitionName,
            'Запущен': this.startDate ? new Date(this.startDate).toLocaleString() : '',
            'Завершён': this.endDate ? new Date(this.endDate).toLocaleString() : '',
            'Статус': this.executionStatus 
        };
    }
}