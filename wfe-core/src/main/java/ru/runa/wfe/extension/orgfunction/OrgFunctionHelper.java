/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package ru.runa.wfe.extension.orgfunction;

import ru.runa.wfe.extension.OrgFunction;

/**
 * Parses and evaluates {@link OrgFunction}'s. Swimlane initializer can be given
 * in 2 forms: 1) FQDN class name(param1, param2, ...) for example
 * 'ru.runa.af.organizationfunction.ExecutorByNameFunction(userName)' 2)
 * 
 * @relationName(FQDN class name(param1, param2, ...)) for example
 *                    '@boss(ru.runa.af.organizationfunction.ExecutorByNameFunction(${processVariab
 *                    l e N a m e } ) ) ' Each param can be given as string or
 *                    as substituted variable name in form of ${userVarName}.
 * 
 * @author Dofs
 * @since 2.0
 * @TODO remove after SwimlaneInitializerHelper tested usage
 */
public class OrgFunctionHelper {
    // private static final String ORG_FUNCTION_PATTERN =
    // "^(\\w+[\\w\\.]*)\\(([^\\)]*)\\)$";
    // private static final Map<String, OrgFunction> CACHE = new HashMap<String,
    // OrgFunction>();
    //
    // /**
    // * @param swimlaneInitializer
    // * @see OrgFunctionHelper
    // * @return not <code>null</code>
    // * @throws OrgFunctionException
    // */
    // public static List<? extends Executor> evaluateOrgFunction(String
    // swimlaneInitializer) throws OrgFunctionException {
    // OrgFunction function = parseOrgFunction(swimlaneInitializer);
    // return evaluateOrgFunction(function, null);
    // }
    //
    // /**
    // *
    // * @param function
    // * @param variableProvider
    // * for substituting organization function parameters
    // * @return not <code>null</code>
    // * @throws OrgFunctionException
    // */
    // public static List<? extends Executor> evaluateOrgFunction(OrgFunction
    // function, VariableProvider variableProvider) throws OrgFunctionException
    // {
    // Object[] parameters = getOrgFunctionParameters(function,
    // variableProvider);
    // List<? extends Executor> result = function.getExecutors(parameters);
    // if (result == null) {
    // result = new ArrayList<Executor>();
    // }
    // return applyRelation(function, result);
    // }
    //
    // private static List<? extends Executor> applyRelation(OrgFunction
    // function, List<? extends Executor> executors) {
    // if (function.getRelationName() == null) {
    // return executors;
    // }
    // ExecutorDao executorDao = ApplicationContextFactory.getExecutorDao();
    // Set<Executor> relationExecutorsSet = new HashSet<Executor>();
    // for (Executor executor : executors) {
    // relationExecutorsSet.add(executor);
    // relationExecutorsSet.addAll(executorDao.getExecutorParentsAll(executor));
    // }
    // Set<Executor> resultSet = Sets.newHashSet();
    // Relation relation =
    // ApplicationContextFactory.getRelationDao().getNotNull(function.getRelationName());
    // List<RelationPair> pairs =
    // ApplicationContextFactory.getRelationPairDao().getExecutorsRelationPairsRight(relation,
    // relationExecutorsSet);
    // for (RelationPair pair : pairs) {
    // resultSet.add(pair.getLeft());
    // }
    // return Lists.newArrayList(resultSet);
    // }
    //
    // private static Object[] getOrgFunctionParameters(OrgFunction orgFunction,
    // VariableProvider variableProvider) {
    // List<Object> params = new ArrayList<Object>();
    // if (orgFunction.getParameterNames() != null) {
    // for (String name : orgFunction.getParameterNames()) {
    // params.addAll(TypeConversionUtil.convertTo(List.class,
    // ExpressionEvaluator.evaluateVariableNotNull(variableProvider, name)));
    // }
    // }
    // Object[] parameters = params.toArray(new Object[params.size()]);
    // return parameters;
    // }
    //
    // /**
    // *
    // * @param swimlaneInitializer
    // * @return
    // * @throws OrgFunctionException
    // */
    // public static OrgFunction parseOrgFunction(String swimlaneInitializer)
    // throws OrgFunctionException {
    // Preconditions.checkNotNull(swimlaneInitializer);
    // OrgFunction orgFunction = CACHE.get(swimlaneInitializer);
    // if (orgFunction == null) {
    // if (swimlaneInitializer.length() == 0) {
    // return new NullOrgFunction();
    // }
    // String relationName = null;
    // if (swimlaneInitializer.startsWith("@")) {
    // int bIndex = swimlaneInitializer.indexOf('(');
    // relationName = swimlaneInitializer.substring(1, bIndex);
    // swimlaneInitializer = swimlaneInitializer.substring(bIndex + 1,
    // swimlaneInitializer.length() - 1);
    // }
    // Pattern functionNamePattern = Pattern.compile(ORG_FUNCTION_PATTERN);
    // Matcher functionNameMatcher =
    // functionNamePattern.matcher(swimlaneInitializer);
    // if (!functionNameMatcher.matches()) {
    // throw new OrgFunctionException("Illegal configuration: '" +
    // swimlaneInitializer + "'");
    // }
    // String functionName = functionNameMatcher.group(1);
    // if (functionName == null) {
    // throw new OrgFunctionException("Illegal or missing function name in " +
    // swimlaneInitializer);
    // }
    // String parameterString = functionNameMatcher.group(2);
    // if (parameterString == null) {
    // throw new OrgFunctionException("Illegal parameter names in " +
    // swimlaneInitializer);
    // }
    // String[] parameterNames = parameterString.length() == 0 ? new String[0] :
    // parameterString.split(",", -1);
    // for (int i = 0; i < parameterNames.length; i++) {
    // if (parameterNames[i].length() == 0) {
    // throw new OrgFunctionException("Illegal parameter name in " +
    // swimlaneInitializer);
    // }
    // }
    // orgFunction =
    // ApplicationContextFactory.createAutowiredBean(functionName);
    // orgFunction.setRelationName(relationName);
    // orgFunction.setParameterNames(parameterNames);
    // CACHE.put(swimlaneInitializer, orgFunction);
    // }
    // return orgFunction;
    // }

}
