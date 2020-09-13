/*
 *      Generates EntityManagerAccess Interface and implementation
 *      Generates BasicFilter class
 *      Modifies repository compilation process adding filtering method
 *      Copyright (C) 2020  Anastasios Gaziotis
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.filtermap;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import org.filtermap.annotations.Filter;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes({"org.filtermap.annotations.Filter"})
public class FilterProcessor extends AbstractProcessor {


    Filer filer;

    Trees trees;

    JavacProcessingEnvironment javacProcessingEnvironment;

    Context context;

    PackageElement packageElement = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        trees = Trees.instance(processingEnv);
        javacProcessingEnvironment = (JavacProcessingEnvironment) processingEnv;
        context = javacProcessingEnvironment.getContext();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private Name getName(String name) {
        return Names.instance(context).fromString(name);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TreeMaker treeMaker = TreeMaker.instance(((JavacProcessingEnvironment) processingEnv).getContext());

        final TreePathScanner< Object, CompilationUnitTree> scanner =
                new TreePathScanner< Object, CompilationUnitTree >() {
                    @Override
                    public Trees visitClass(final ClassTree classTree,
                                            final CompilationUnitTree unitTree) {

                        if (unitTree instanceof JCTree.JCCompilationUnit) {
                            final JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit)unitTree;

                            // Only process on files which have been compiled from source
                            if (compilationUnit.sourcefile.getKind() == JavaFileObject.Kind.SOURCE) {
                                compilationUnit.accept(new TreeTranslator() {
                                    public void visitMethodDef(final JCTree.JCMethodDecl tree) {
                                        super.visitMethodDef(tree);

                                        Element classElement = processingEnv.getElementUtils().getTypeElement(unitTree.getPackageName() + "." + classTree.getSimpleName());
                                        ExecutableElement methodElement = (ExecutableElement) classElement.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.METHOD && (e.getSimpleName()).equals((tree.name))).findFirst().get();
                                        if (methodElement.getAnnotation(Filter.class) == null) return;

                                        String entityManagerAccessClassName = compilationUnit.getPackageName().toString()+".EntityManagerAccess";
                                        String[] entityManagerAccessStrings = entityManagerAccessClassName.split("\\.");
                                        JCTree.JCExpression entityManagerAccessClassNameIdent = treeMaker.Ident(getName(entityManagerAccessStrings[0]));
                                        for (int i = 1; i < entityManagerAccessStrings.length; i++) {
                                            entityManagerAccessClassNameIdent = treeMaker.Select(entityManagerAccessClassNameIdent, getName(entityManagerAccessStrings[i]));
                                        }

                                        JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) classTree;
                                        boolean implemented = false;
                                        for (JCTree.JCExpression implementedInterface : classDecl.implementing)
                                            if (implementedInterface.toString().equals(entityManagerAccessClassNameIdent.toString()))
                                                implemented = true;

                                        if (!implemented)
                                            classDecl.implementing = classDecl.implementing.appendList(List.<JCTree.JCExpression>of(entityManagerAccessClassNameIdent));

                                        Map<String, Integer> parameterPositions = new HashMap();
                                        int position = 0;
                                        for (VariableElement p : methodElement.getParameters()) {
                                            parameterPositions.put(p.asType().toString(), position);
                                            position++;
                                        }

                                        JCTree.JCIdent getEntityManager = treeMaker.Ident(getName("accessEntityManager"));
                                        JCTree.JCMethodInvocation getEntityManagerInvocation =
                                                treeMaker.Apply(List.<JCTree.JCExpression>nil(), getEntityManager, List.<JCTree.JCExpression>nil());
                                        JCTree.JCExpressionStatement execGetEntityManager = treeMaker.Exec(getEntityManagerInvocation);

                                        ArrayList<JCTree.JCStatement> statementsList = new ArrayList<>();

                                        String className = compilationUnit.getPackageName().toString()+".BasicFilter";
                                        String[] strings = className.split("\\.");
                                        JCTree.JCExpression classNameIdent = treeMaker.Ident(getName(strings[0]));
                                        for (int i = 1; i < strings.length; i++) {
                                            classNameIdent = treeMaker.Select(classNameIdent, getName(strings[i]));
                                        }
                                        JCTree.JCNewClass classObj = treeMaker.NewClass(null, List.<JCTree.JCExpression>nil(), classNameIdent,
                                                List.<JCTree.JCExpression>of(treeMaker.ClassLiteral(tree.restype.type.getTypeArguments().get(0)),
                                                        execGetEntityManager.expr), null);

                                        JCTree.JCFieldAccess readValue = treeMaker.Select(classObj, getName("filter"));
                                        JCTree.JCMethodInvocation filterInvocation =
                                                treeMaker.Apply(List.<JCTree.JCExpression>nil(), readValue, List.<JCTree.JCExpression>of(treeMaker.Ident(tree.params.get(parameterPositions.get("java.util.Map")).name)));
                                        JCTree.JCExpression execFilter = treeMaker.Exec(filterInvocation).getExpression();
                                        JCTree.JCVariableDecl variableDecl = treeMaker.VarDef(new Symbol.VarSymbol(0, getName("basicFilter"), classNameIdent.type, tree.sym), execFilter);
                                        statementsList.add(variableDecl);

                                        if(parameterPositions.get("org.filtermap.FilterMapSort") != null && tree.params.get(parameterPositions.get("org.filtermap.FilterMapSort")).vartype.toString().equals("FilterMapSort")) {
                                            JCTree.JCFieldAccess sort = treeMaker.Select(treeMaker.Ident(getName("basicFilter")), getName("sort"));
                                            JCTree.JCMethodInvocation sortInvocation =
                                                    treeMaker.Apply(List.<JCTree.JCExpression>nil(), sort, List.<JCTree.JCExpression>of(treeMaker.Ident((tree.params.get(parameterPositions.get("org.filtermap.FilterMapSort")).name))));
                                            JCTree.JCExpressionStatement execSort = treeMaker.Exec(sortInvocation);
                                            statementsList.add(execSort);
                                        }

                                        if(parameterPositions.get("org.filtermap.FilterMapPaginate") != null && tree.params.get(parameterPositions.get("org.filtermap.FilterMapPaginate")).vartype.toString().equals("FilterMapPaginate")) {
                                            JCTree.JCFieldAccess paginate = treeMaker.Select(treeMaker.Ident(getName("basicFilter")), getName("page"));
                                            JCTree.JCMethodInvocation pageInvocation =
                                                    treeMaker.Apply(List.<JCTree.JCExpression>nil(), paginate, List.<JCTree.JCExpression>of(treeMaker.Ident((tree.params.get(parameterPositions.get("org.filtermap.FilterMapPaginate")).name))));
                                            JCTree.JCExpressionStatement execPage = treeMaker.Exec(pageInvocation);
                                            statementsList.add(execPage);
                                        }

                                        JCTree.JCFieldAccess get = treeMaker.Select(treeMaker.Ident(getName("basicFilter")), getName("get"));
                                        JCTree.JCMethodInvocation getInvocation =
                                                treeMaker.Apply(List.<JCTree.JCExpression>nil(), get, List.<JCTree.JCExpression>nil());
                                        JCTree.JCExpressionStatement execGet = treeMaker.Exec(getInvocation);


                                        JCTree.JCReturn returnStatement = treeMaker.Return(execGet.expr);
                                        statementsList.add(returnStatement);

                                        List<JCTree.JCStatement> statements = List.<JCTree.JCStatement>from(statementsList);

                                        tree.body = treeMaker.Block(0, statements);
                                        tree.mods.flags |= Flags.DEFAULT;

                                     }
                                });
                            }
                        }

                        return trees;
                    }
                };

        boolean filterCreated = false;
        boolean entityManagerAccessCreated = false;

        for(final Element element: roundEnv.getElementsAnnotatedWith(Filter.class)) {

            Element enclosingElement = element.getEnclosingElement();

            if (filterCreated == false)
                    filterCreated = createFilter(enclosingElement);

            if (entityManagerAccessCreated == false)
                entityManagerAccessCreated = createEntityManagerAccessInterface(enclosingElement);


            final TreePath path = trees.getPath(enclosingElement);
            scanner.scan(path, path.getCompilationUnit());
            continue;
        }

        return false;

    }

    private boolean createEntityManagerAccessInterface(Element element) {
        packageElement = (PackageElement) element.getEnclosingElement();
        JavaFileObject file = null;

        JavaFileObject fileImpl = null;
        try {

            file = filer.createSourceFile(packageElement.getQualifiedName() +".EntityManagerAccess");
            BufferedWriter bufferedWriter = new BufferedWriter(file.openWriter());

            bufferedWriter.write("/*\n" +
                    " *      Creates an interface for accessing the entity manager.\n" +
                    " *      Copyright (C) 2020  Anastasios Gaziotis\n" +
                    " *\n" +
                    " *      This program is free software: you can redistribute it and/or modify\n" +
                    " *      it under the terms of the GNU General Public License as published by\n" +
                    " *      the Free Software Foundation, either version 3 of the License, or\n" +
                    " *      (at your option) any later version.\n" +
                    " *\n" +
                    " *      This program is distributed in the hope that it will be useful,\n" +
                    " *      but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                    " *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                    " *      GNU General Public License for more details.\n" +
                    " *\n" +
                    " *      You should have received a copy of the GNU General Public License\n" +
                    " *      along with this program.  If not, see <https://www.gnu.org/licenses/>.\n" +
                    "*/\n" +
                    "package "+packageElement.getQualifiedName()+";\n" +
                    "\n" +
                    "import javax.persistence.*;\n" +
                    "\n" +
                    "public interface EntityManagerAccess {\n" +
                    "\n" +
                    "    public EntityManager accessEntityManager();\n" +
                    "}");

            bufferedWriter.close();

            file = filer.createSourceFile(packageElement.getQualifiedName() +".EntityManagerAccessImpl");
            bufferedWriter = new BufferedWriter(file.openWriter());

            bufferedWriter.write("/*\n" +
                    " *      Creates an interface for accessing the entity manager.\n" +
                    " *      Copyright (C) 2020  Anastasios Gaziotis\n" +
                    " *\n" +
                    " *      This program is free software: you can redistribute it and/or modify\n" +
                    " *      it under the terms of the GNU General Public License as published by\n" +
                    " *      the Free Software Foundation, either version 3 of the License, or\n" +
                    " *      (at your option) any later version.\n" +
                    " *\n" +
                    " *      This program is distributed in the hope that it will be useful,\n" +
                    " *      but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                    " *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                    " *      GNU General Public License for more details.\n" +
                    " *\n" +
                    " *      You should have received a copy of the GNU General Public License\n" +
                    " *      along with this program.  If not, see <https://www.gnu.org/licenses/>.\n" +
                    "*/\n" +
                    "package "+packageElement.getQualifiedName()+";\n" +
                    "\n" +
                    "import javax.persistence.*;\n" +
                    "\n" +
                    "public class EntityManagerAccessImpl implements EntityManagerAccess {\n" +
                    "\n" +
                    "   @PersistenceContext\n" +
                    "   private EntityManager em;\n" +
                    "\n" +
                    "   public EntityManager accessEntityManager() {\n" +
                    "       return em;\n" +
                    "   }\n" +
                    "}");

            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean createFilter(Element element) {
        PackageElement packageElement = (PackageElement) element.getEnclosingElement();
        JavaFileObject file = null;
        try {

            file = filer.createSourceFile(packageElement.getQualifiedName() +".BasicFilter");
            BufferedWriter bufferedWriter = new BufferedWriter(file.openWriter());

            bufferedWriter.write("/*\n" +
                    " *      Creates a the logic for filtering database results.\n" +
                    " *      Copyright (C) 2020  Anastasios Gaziotis\n" +
                    " *\n" +
                    " *      This program is free software: you can redistribute it and/or modify\n" +
                    " *      it under the terms of the GNU General Public License as published by\n" +
                    " *      the Free Software Foundation, either version 3 of the License, or\n" +
                    " *      (at your option) any later version.\n" +
                    " *\n" +
                    " *      This program is distributed in the hope that it will be useful,\n" +
                    " *      but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                    " *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                    " *      GNU General Public License for more details.\n" +
                    " *\n" +
                    " *      You should have received a copy of the GNU General Public License\n" +
                    " *      along with this program.  If not, see <https://www.gnu.org/licenses/>.\n" +
                    "*/\n" +
                    "package "+packageElement.getQualifiedName()+";\n" +
                    "\n" +
                    "import javax.persistence.*;\n" +
                    "import javax.persistence.criteria.*;\n" +
                    "import java.util.*;\n" +
                    "import javax.swing.*;\n" +
                    "import org.filtermap.FilterMapSort;\n" +
                    "import org.filtermap.FilterMapPaginate;\n" +
                    "\n" +
                    "public class BasicFilter<T> {\n" +
                    "\n" +
                    "    private EntityManager entityManager;\n" +
                    "    \n" +
                    "    private CriteriaBuilder builder;\n" +
                    "    \n" +
                    "    private CriteriaQuery<T> query;\n" +
                    "\n" +
                    "    private Root<T> root;\n" +
                    "\n" +
                    "    private Class filterClass;\n" +
                    "\n" +
                    "    private Integer page = 1;\n" +
                    "\n" +
                    "    private final List<Order> orderList = new ArrayList<>();\n" +
                    "\n" +
                    "    private Integer pageSize = Integer.MAX_VALUE;\n" +
                    "\n" +
                    "    public BasicFilter(Class filterClass, EntityManager em) {\n" +
                    "        this.filterClass = filterClass;\n" +
                    "        this.entityManager = em;\n" +
                    "        initialize();\n" +
                    "    }\n" +
                    "\n" +
                    "    public void initialize() {\n" +
                    "        this.builder = this.entityManager.getCriteriaBuilder();\n" +
                    "        this.query = this.builder.createQuery(this.filterClass);\n" +
                    "        this.root = this.query.from(this.filterClass);\n" +
                    "    }\n" +
                    "\n" +
                    "    public BasicFilter<T> filter(Map filterObject) {\n" +
                    "\n" +
                    "       Predicate predicate = null;\n" +
                    "\n" +
                    "            for (Object o : filterObject.entrySet()) {\n" +
                    "                Predicate subpredicate = null;\n" +
                    "                Map.Entry filter = (Map.Entry) o;\n" +
                    "                String key = filter.getKey().toString();\n" +
                    "                if (filter.getValue() == null) continue;\n" +
                    "\n" +
                    "                subpredicate = this.root.get(key).in(((List) filter.getValue()));\n" +
                    "\n" +
                    "                if (predicate == null) {\n" +
                    "                    predicate = subpredicate;\n" +
                    "                } else {\n" +
                    "                    predicate = this.builder.or(predicate, subpredicate);\n" +
                    "                }\n" +
                    "            }\n" +
                    "\n" +
                    "            if (predicate != null) this.query.where(predicate);\n" +
                    "\n" +
                    "        return this;\n" +
                    "    }\n" +
                    "\n" +
                    "    public BasicFilter<T> sort(FilterMapSort filterMapSort) {\n" +
                    "        if (filterMapSort.direction == SortOrder.ASCENDING)\n" +
                    "            return this.sort(filterMapSort.fields);\n" +
                    "         else if (filterMapSort.direction == SortOrder.DESCENDING)\n" +
                    "            filterMapSort.fields.stream().map(field -> this.builder.desc(this.root.get(field))).forEachOrdered(this.orderList::add);\n" +
                    "        return this;\n" +
                    "    }\n" +
                    "\n" +
                    "    private BasicFilter<T> sort(List<String> fields) {\n" +
                    "        fields.stream().map(field -> this.builder.asc(this.root.get(field))).forEachOrdered(this.orderList::add);\n" +
                    "        return this;\n" +
                    "    }\n" +
                    "\n" +
                    "    public BasicFilter<T> page(FilterMapPaginate filterMapPaginate) {\n" +
                    "        this.page = filterMapPaginate.page;\n" +
                    "        this.pageSize = filterMapPaginate.pageSize;\n" +
                    "        return this;\n" +
                    "    }\n" +
                    "\n" +
                    "    public List<T> get() {\n" +
                    "        this.query.orderBy(this.orderList);\n" +
                    "        TypedQuery<T> typedQuery = this.entityManager.createQuery(this.query);\n" +
                    "        typedQuery.setFirstResult((this.page-1) * this.pageSize);\n" +
                    "        typedQuery.setMaxResults(this.pageSize);\n" +
                    "        return typedQuery.getResultList();\n" +
                    "    }\n" +
                    "}");

                bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
