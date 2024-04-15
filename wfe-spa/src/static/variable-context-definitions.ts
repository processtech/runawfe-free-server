import type { VariableContextDefinition } from '../ts/variable-context-definition'

export const variableContextDefinitions : { [key: string]: VariableContextDefinition } = {
  string: {
    defaultValue: () => '',
    componentName: 'string-variable-format',
  },
  integer: {
    defaultValue: () => 0,
    componentName: 'number-variable-format',
  },
  bigdecimal: {
    defaultValue: () => 0,
    componentName: 'number-variable-format',
  },
  double: {
    defaultValue: () => 0,
    componentName: 'number-variable-format',
  },
  date: {
    defaultValue: () => new Date().getTime(),
    componentName: 'date-variable-format',
  },
  datetime: {
    defaultValue: () => new Date().getTime(),
    componentName: 'date-time-variable-format',
  },
  time: {
    defaultValue: () => new Date().getTime(),
    componentName: 'time-variable-format',
  },
  executor: {
    defaultValue: () => null, // TODO
    componentName: 'executor-variable-format',
  },
  file: {
    defaultValue: () => null, //TODO
    componentName: 'file-variable-format'
  },
  text: {
    defaultValue: () => '',
    componentName: 'text-variable-format'
  },
  list: {
    defaultValue: () => [],
    componentName: 'list-variable-format',
  },
  processref: {
    defaultValue: () => 0, // TODO,
    componentName: 'process-variable-format',
  },
  formattedText: {
    defaultValue: () => '',
    componentName: 'formatted-text-variable-format'
  },
  boolean: {
    defaultValue: () => false,
    componentName: 'boolean-variable-variable-format',
  },
  userType: {
    defaultValue: () => null,
    componentName: 'user-type-variable-format',
  },
}
