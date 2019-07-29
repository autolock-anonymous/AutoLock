package sip4j.parser;

import sip4j.datastructure.E_Class;
import sip4j.datastructure.E_Method;
import sip4j.datastructure.E_Object;
import sip4j.datastructure.E_Package;
import sip4j.datautilities.Data_Controller;
import sip4j.datautilities.Data_Generator;
import org.eclipse.jdt.core.dom.*;

import java.util.List;
public class AST_Visitor extends ASTVisitor {
	private String filename;

	public AST_Visitor() {
		super();
	}

	public AST_Visitor(String filename){
		super();
		this.filename = filename;
	}

	@Override
	public void preVisit(ASTNode node) {
		super.preVisit(node);
	}
	@Override
	public void postVisit(ASTNode node) {
		super.postVisit(node);
	}
	/**
	 * create new package
 	 */
	@Override
	public boolean visit(PackageDeclaration node) {
		E_Package pack = Data_Generator.getPackage();
		pack.setName(node.getName().toString());
		return super.visit(node);
	}
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return super.visit(node);
	}
	/**
	 * create class or interface
 	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		//create new class
		E_Class _class = null;
		if(node.resolveBinding() != null && ! node.isInterface()){
			_class = AST_Parser.createNewClass(node.resolveBinding());
			_class.setFilename(filename);
			// add class to a package
			//c
			AST_Parser.addClassToPackage(Data_Generator.getPackage(), _class);
			E_Object obj = new E_Object();
			//C*c
			E_Method constructor = AST_Parser.createDefaultConstructor(node, obj);
			AST_Parser.addClassMethod(_class, constructor);
			AST_Parser.addClassToPackage(Data_Generator.getPackage(), _class);

			/// get Class fields
			FieldDeclaration[] fields = node.getFields();
			// create a Field data structure and add field in a class
			AST_Parser.addClassFields(_class, fields, constructor);
			/*if(mainClass!=null){
				_mainClass = AST_Parser.createNewClass(mainClass.resolveBinding());
				if(_mainClass!=null){
					//Data_Generator.getPackage().getClasses().addLast(_mainClass);
					AST_Parser.addPackageClass(Data_Generator.getPackage(),_mainClass);
					AST_Parser.addClassMethod(_mainClass, mainMethod);
				}
			}*/
			/// fetch all method invocations in a class
			List<MethodInvocation> invokedMethods = AST_Parser.getMethodInvocations(node);
			if(!(invokedMethods.isEmpty())){
				for(MethodInvocation inv:invokedMethods){
					/// System.out.println("Method inv = "+inv.getName()+" for method"+node.getName());
					AST_Parser.addMethodInvocationExp(inv);
				}
			}
		}
		return super.visit(node);
	}
	@Override
	public boolean visit(MethodDeclaration node) {
//		expression.
//		statement.setExpression();


		/// System.out.println("In method = "+node.getName().toString());
		E_Object obj = new E_Object();
		if(node.resolveBinding() != null && node.getBody() != null){
			AST_Parser.addMethodData(node, obj);
		}
		return super.visit(node);
	}
	@Override
	public boolean visit(final Initializer bNode) {
		//TypeDeclaration initClass = null;
		//E_Method method = null;
		if(bNode.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			bNode.getBody().accept(new ASTVisitor() {
				@Override
				public boolean visit(Assignment node) {
					Expression laExp = node.getLeftHandSide();
					Expression raExp = node.getRightHandSide();
					//E_Method _method = Data_Controller.searchMethod(AST_Parser.fetchParentMethodDecl(node));
					//E_Class _mainClass = null;
					//HashMap<TypeDeclaration, MethodDeclaration> mainMap = AST_Parser.getMainDeclarationFromProject((TypeDeclaration)bNode.getParent());
//					*f(mainMap != null && mainMap.isEmpty() == false){
//						Set mapSet = (Set) mainMap.entrySet();
//						Iterator mapIterator = mapSet.iterator();
//						while (mapIterator.hasNext()) {
//				            Map.Entry mapEntry = (Map.Entry) mapIterator.next();
//				            // getKey Method of HashMap access a key of map
//				            mainClass = (TypeDeclaration) mapEntry.getKey();
//				            //getValue method returns corresponding key's value
//				            mainDecl = (MethodDeclaration) mapEntry.getValue();
//				            //System.out.println("Key : " + mainClass.getName().toString() + "= Value : " + mainDecl.getName());
//						}
//					}
//					else{
					MethodDeclaration thisMethod = null;
					E_Method method = null;
					TypeDeclaration initClass = null;
					List<ASTNode> children = AST_Parser.getChildren(node);
					for (ASTNode child: children) {
						if (child.getNodeType() == ASTNode.METHOD_DECLARATION){
							MethodDeclaration md = (MethodDeclaration) child;
							IMethodBinding tmb = md.resolveBinding();
							if (AST_Parser.ifUserDefinedMethod(tmb)){
								if(tmb != null){
									thisMethod = md;
									break;
								}
								//else part of method here
							}
						}
					}
					// if method is given as a part of initializer
					if(thisMethod != null){
						E_Method m = Data_Controller.searchMethod(thisMethod);
						if(m != null){
							method = m;
						}
						else{
							E_Object obj = new E_Object();
							method =  AST_Parser.createNewMethod(thisMethod,obj);
						}
					}
					else{
						initClass = (TypeDeclaration) bNode.getParent();
						method = Data_Controller.searchConstMethod(initClass);
					}
					if(laExp != null && method!=null){
						if (laExp.getNodeType() == ASTNode.FIELD_ACCESS || laExp.getNodeType() == ASTNode.SIMPLE_NAME ||
								laExp.getNodeType() == ASTNode.QUALIFIED_NAME){
							//||laExp.getNodeType() == ASTNode.ARRAY_ACCESS) {
							AST_Parser.addAssignmentExpression(laExp,raExp,method);
						}
						else if(laExp.getNodeType() == ASTNode.SIMPLE_TYPE){
							System.out.println("SimpleType "+node.getProperty("name"));
						}
						else {
							List<ASTNode> l_children = AST_Parser.getChildren(laExp);
							for (ASTNode l_child: l_children) {
								if (l_child.getNodeType() == ASTNode.FIELD_ACCESS || l_child.getNodeType() == ASTNode.SIMPLE_NAME
										|| l_child.getNodeType() == ASTNode.QUALIFIED_NAME
										||l_child.getNodeType() == ASTNode.ARRAY_ACCESS) {
									Expression childExp = (Expression) l_child;
									AST_Parser.addAssignmentExpression(childExp, raExp, method);
								}
							}
						}
					}
					// This is a special treatment for double dimension arrays
					if (laExp.getNodeType() == ASTNode.ARRAY_ACCESS) {
						ArrayAccess f = (ArrayAccess) laExp;
						List<ASTNode> l_children = AST_Parser.getChildren(f.getArray());
						for (ASTNode child: l_children) {
							if (child.getNodeType() == ASTNode.FIELD_ACCESS || child.getNodeType() == ASTNode.SIMPLE_NAME ||
									child.getNodeType() == ASTNode.QUALIFIED_NAME) {
								Expression child_exp = (Expression) child;
								AST_Parser.checkRightSide(child_exp,raExp,method);
							}
						}
					}
					return super.visit(node);
				}
			});
		}
		return super.visit(bNode);
	}
}