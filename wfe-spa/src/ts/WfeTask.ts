import { WfeExecutor } from './WfeExecutor';
import { WfeVariable } from './WfeVariable';

export class WfeTask {
    id: number|null = null;
    name: string = '';
    category: string = '';
    nodeId: string = '';
    description: string = '';
    swimlaneName: string = '';
    owner: WfeExecutor|null = null;
    targetActor: WfeExecutor|null = null;
    definitionVersionId: number|null = null;
    definitionName: string = '';
    processId: number|null = null;
    processHierarchyIds: string = '';
    tokenId: number|null = null;
    createDate: Date|null = null;
    deadlineDate: Date|null = null;
    deadlineWarningDate: Date|null = null;
    assignDate: Date|null = null;
    escalated: boolean|null = null;
    firstOpen: boolean|null = null;
    acquiredBySubstitution: boolean|null = null;
    multitaskIndex: number|null = null;
    readOnly: boolean|null = null;
    variables: Array<WfeVariable>|null = null;

    getInfo(): object {
        return {
            'Имя процесса': this.definitionName,
            'Имя': this.name,
            'Описание': this.description,
            'Номер экземпляра процесса': this.processId,
            'Исполнитель': this.targetActor ? this.targetActor.fullName : '',
            'Роль': this.swimlaneName ? this.swimlaneName : '',
            'Создана': this.createDate ? new Date(this.createDate).toLocaleString() : '',
            'Время окончания': this.deadlineDate ? new Date(this.deadlineDate).toLocaleString() : ''
        };
    }
}