export class Process {
    id: number|null = null;
    name: string = '';
    executionStatus: string = '';
    startDate: Date|null = null;
    endDate: Date|null = null; 

    getProcessInfo(): object {
        return {
            'Номер': this.id,
            'Имя процесса': this.name,
            'Запущен': this.startDate ? new Date(this.startDate).toLocaleString() : '',
            'Завершён': this.endDate ? new Date(this.endDate).toLocaleString() : '',
            'Статус': this.executionStatus 
        };
    }
}