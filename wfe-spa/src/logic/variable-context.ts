import { variableContextDefinitions } from '../static/variable-context-definitions';
import type { VariableContextDefinition } from '../ts/variable-context-definition'

export class VariableContext {
  private readonly context: VariableContextDefinition;

  constructor(formatName: string) {
    if (variableContextDefinitions[formatName]) {
      this.context = variableContextDefinitions[formatName];
    } else {
      this.context = variableContextDefinitions.userType
    }
  }

  defaultValue(): any {
    return this.context.defaultValue();
  }

  componentName(): string {
    return this.context.componentName;
  }
}
