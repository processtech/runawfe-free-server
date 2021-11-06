import { ExecutorDto } from './ExecutorDto';

export class WfDefinitionDto {
    id: number|null = null;
    versionId: number|null = null;
    name: string = '';
    description: string = '';
    categories: Array<string>|null = '';
    version: number|null = null;
    canBeStarted: boolean = false;
    createDate: Date|null = null;
    createActor: ExecutorDto|null = null;
    updateDate: Date|null = null;
    updateActor: ExecutorDto|null = null;

    getInfo(): object {
        return {
            'ID': this.versionId,
            'Имя': this.name,
            'Описание': this.description,
            'Создана': this.createDate ? new Date(this.createDate).toLocaleString() : '',
            'Автор создания': this.createActor ? this.createActor.fullName : '',
            'Обновлена': this.updateDate ? new Date(this.updateDate).toLocaleString() : '',
            'Автор обновления': this.updateActor ? this.updateActor.fullName : '',
        };
    }
}