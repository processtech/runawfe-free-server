export class WfReportDto {
    id: number|null = null;
    name: string = '';
    description: string = '';
    category: string = '';
    parameters: Array<WfReportParameterDto>|null = null;

    getInfo(): object {
        return {
            'Название': this.name,
            'Описание': this.description
        };
    }
}
class WfReportParameterDto{
	userName: string = '';
    description: string = '';
    internalName: string = '';
    position: number|null = null;
    type: any;
    required: boolean|null = null;
    value: any = '';
}