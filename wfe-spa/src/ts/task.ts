import { Executor } from './executor';

export class Task {
    id: number|null = null;
    name: string = '';
    category: string = '';
    nodeId: string = '';
    description: string = '';
    swimlaneName: string = '';
    owner: Executor|null = null;
    targetActor: Executor|null = null;
    definitionVersionId: number|null = null;
    definitionName: string = '';
    processId: number|null = null;
    processHierarchyIds: string = '';
    tokenId: number|null = null;
    creationDate: Date|null = null;
    deadlineDate: Date|null = null;
    deadlineWarningDate: Date|null = null;
    assignDate: Date|null = null;
    escalated: boolean|null = null;
    firstOpen: boolean|null = null;
    acquiredBySubstitution: boolean|null = null;
    multitaskIndex: number|null = null;
    readOnly: boolean|null = null;
    variables: Array<Variable>|null = null;

    getTaskInfo(): object {
        return {
            'Имя процесса': this.definitionName,
            'Имя': this.name,
            'Описание': this.description,
            'Номер экземпляра процесса': this.processId,
            'Владелец': this.owner ? this.owner.fullName : '',
            'Роль': this.targetActor ? this.targetActor.fullName : '',
            'Создана': this.creationDate ? new Date(this.creationDate).toLocaleString() : '',
            'Время окончания': this.deadlineDate ? new Date(this.deadlineDate).toLocaleString() : ''
        };
    }
}

class Variable {
    name: string = '';
    value: any;
}