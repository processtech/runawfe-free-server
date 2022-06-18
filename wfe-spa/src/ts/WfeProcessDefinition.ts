import { WfeExecutor } from './WfeExecutor';

export class WfeProcessDefinition {
    id: number|null = null;
    name: string = '';
    description: string = '';
    categories: Array<string>|null = '';
    version: number|null = null;
    canBeStarted: boolean = false;
    createDate: Date|null = null;
    createActor: WfeExecutor|null = null;
    updateDate: Date|null = null;
    updateActor: WfeExecutor|null = null;

    getInfo(): object {
        return {
            'ID': this.id,
            'Имя': this.name,
            'Описание': this.description,
            'Создана': this.createDate ? new Date(this.createDate).toLocaleString() : '',
            'Автор создания': this.createActor ? this.createActor.fullName : '',
            'Обновлена': this.updateDate ? new Date(this.updateDate).toLocaleString() : '',
            'Автор обновления': this.updateActor ? this.updateActor.fullName : '',
        };
    }
}