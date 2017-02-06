package airptool.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.Block;

import airptool.enums.DependencyType;

public class DependencyVisitor extends ASTVisitor {
	private List<Object[]> dependenciesCP;
	private List<Object[]> dependenciesMC;
	private List<Object[]> dependenciesBM;

	private ICompilationUnit unit;
	private CompilationUnit fullClass;
	private CompilationUnit fullMethod;
	private String className;
	private boolean MC, methodInterFirstBlock, interno;
	private int count2, count3;
	private int bloco;
	private ArrayList<Integer> linelist;
	private ArrayList<Integer> blist;
	private ArrayList<String> blocosExternos;
	private ArrayList<String> metodoExterno;
	private int count, nextvalue, lineMethod, lineBlock;
	private String methodName;
	private String classMethodName;
	private String blockIndex;

	public DependencyVisitor(ICompilationUnit unit) throws JavaModelException {
		this.dependenciesCP = new ArrayList<Object[]>();
		this.dependenciesMC = new ArrayList<Object[]>();
		this.dependenciesBM = new ArrayList<Object[]>();
		this.unit = unit;
		this.MC = false;
		this.interno = false;
		this.count2=0;
		this.count3=0;
		this.methodInterFirstBlock=false;
		this.bloco=0;
		this.blist= new ArrayList<Integer>();
		this.linelist= new ArrayList<Integer>();
		this.count=1;
		this.lineMethod = 0;
		this.lineBlock = 0;
		this.nextvalue = 1;
		this.methodName = "";
		this.classMethodName = "";
		this.blockIndex = "";
		this.blocosExternos = new ArrayList<String>();
		this.metodoExterno = new ArrayList<String>();
		
	
		this.className = unit.getParent().getElementName() + "." + unit.getElementName().substring(0, unit.getElementName().length() - 5);
		ASTParser parser = ASTParser.newParser(AST.JLS4); // It was JSL3, but it
															// is now deprecated
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);

		this.fullClass = (CompilationUnit) parser.createAST(null);// parse
		this.fullClass.accept(this);
		
		IType[] allTypes = unit.getAllTypes();
		for(IType type : allTypes){
			IMethod[] allMethods = type.getMethods();
			for(IMethod method : allMethods){
				final ICompilationUnit methodUnit = method.getCompilationUnit();
				IType it = null;
				try{
					IType[] types = methodUnit.getAllTypes();
					IType[] types2 = methodUnit.getTypes();
					
						for(IType t : types){
							IMethod[] methods = t.getMethods();
							int v=1;
						}
				} catch (JavaModelException e){
					e.printStackTrace();
				}
			}
				
				/*
				this.className = method.getElementName();
				int methodStart = method.getSourceRange().getOffset();
				int methodLen = method.getSourceRange().getLength();
				String temp3 = methodUnit.getSource().substring(methodStart, (methodStart+methodLen));
				
				ASTParser parser = ASTParser.newParser(AST.JLS4); // It was JSL3, but it
				// is now deprecated
				parser.setKind(ASTParser.K_COMPILATION_UNIT);
				//parser.setSource(methodUnit.getSource().substring(methodStart, (methodStart+methodLen)).toCharArray());
				//parser.setFocalPosition(methodStart);
				//parser.setSourceRange(methodStart, methodLen);
				parser.setCompilerOptions(JavaCore.getOptions());
				parser.setProject(unit.getJavaProject());
				parser.setUnitName(this.className);
				parser.setResolveBindings(true);
				parser.setBindingsRecovery(true);
				

				this.fullMethod = (CompilationUnit) parser.createAST(null);
				this.fullMethod.accept(this);
		*/		
		}
		
	}

	public final List<Object[]> getDependenciesCP() {
		return this.dependenciesCP;
	}
	
	public final List<Object[]> getDependenciesMC() {
		return this.dependenciesMC;
	}
	
	public final List<Object[]> getDependenciesBM() {
		return this.dependenciesBM;
	}

	public final String getClassName() {
		return this.className;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (!node.isLocalTypeDeclaration() && !node.isMemberTypeDeclaration()) { // Para
																					// evitar
																					// fazer
																					// v�rias
																					// vezes
			try {
				IType type = (IType) unit.getTypes()[0];
				ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(null);

				IType[] typeSuperclasses = typeHierarchy.getAllSuperclasses(type);

				for (IType t : typeSuperclasses) {
					if (node.getSuperclassType() != null
							&& t.getFullyQualifiedName().equals(node.getSuperclassType().resolveBinding().getQualifiedName())) {
						if(MC) this.dependenciesMC.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName(), methodName, getClassName(), lineMethod});
						
						if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
									this.dependenciesBM.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
								}else{
									this.dependenciesBM.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
								}
							}
						}
						this.dependenciesCP.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName()});
					} else {
						// this.dependencies.add(new Object[] {
						// DependencyType.EXTEND, t.getFullyQualifiedName() });
					}
				}

				IType[] typeSuperinter = typeHierarchy.getAllInterfaces();

				externo: for (IType t : typeSuperinter) {
					for (Object it : node.superInterfaceTypes()) {
						switch (((Type) it).getNodeType()) {
						case ASTNode.SIMPLE_TYPE:
							SimpleType st = (SimpleType) it;
							if (t.getFullyQualifiedName().equals(st.getName().resolveTypeBinding().getQualifiedName())) {
								if (!type.isInterface()) {
									if(MC) this.dependenciesMC.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName(), methodName, getClassName(), lineMethod});
									
									if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
											this.dependenciesBM.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
										} else {
											this.dependenciesBM.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
											}
										}
									}
									
									this.dependenciesCP.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName() });
								} else {
									if(MC) this.dependenciesMC.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName(), methodName, getClassName(), lineMethod});
									
									if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
											this.dependenciesBM.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
										}else{
											this.dependenciesBM.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
										}
										}
									}
									
									this.dependenciesCP.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName() });
								}
								continue externo;
							}
							break;
						case ASTNode.PARAMETERIZED_TYPE:
							ParameterizedType pt = (ParameterizedType) it;
							if (t.getFullyQualifiedName().equals(pt.getType().resolveBinding().getBinaryName())) {
								if (!type.isInterface()) {
									if(MC) this.dependenciesMC.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName(), methodName, getClassName(), lineMethod});
									
									if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
											this.dependenciesBM.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
										} else{
											this.dependenciesBM.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
											}
										}
									}
									
									this.dependenciesCP.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName() });
								} else {
									if(MC) this.dependenciesMC.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName(), methodName, getClassName(), lineMethod});
									
									if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
											 this.dependenciesBM.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
										}else {
											this.dependenciesBM.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
											}
										}
									}
									this.dependenciesCP.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName() });
								}
								continue externo;
							}
							break;
						}
					}
					// this.dependencies.add(new Object[] {
					// DependencyType.IMPLEMENT, t.getFullyQualifiedName() });
				}
			} catch (JavaModelException e) {
				throw new RuntimeException("AST Parser error.", e);
			}
		}
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
			// FieldDeclaration field = (FieldDeclaration) node.getParent();
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			
			this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		
		} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			// MethodDeclaration method = (MethodDeclaration) node.getParent();
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(), lineMethod});

			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}else{
						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
						}
					}
			}

			this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		
		} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(), lineMethod});

			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					 this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			
			this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		
		} else if (node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			// VariableDeclarationStatement st = (VariableDeclarationStatement)
			// node.getParent();
			// VariableDeclarationFragment vdf = ((VariableDeclarationFragment)
			// st.fragments().get(0));
			ASTNode relevantParent = this.getRelevantParent(node);
			if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
				// MethodDeclaration md = (MethodDeclaration) relevantParent;
				if(MC) this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(), lineMethod});
				
				if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
									this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}else{
						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
						}
					}
				}
				
				this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
			}
		} else if (node.getParent().getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
			// SingleVariableDeclaration sv = (SingleVariableDeclaration)
			// node.getParent();
			ASTNode relevantParent = this.getRelevantParent(node);
			if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
				// MethodDeclaration md = (MethodDeclaration) relevantParent;
				if(MC) this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(), lineMethod});
				
				if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}else{
						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
						}
					}
				}
				
				this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
			}

		}
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
			// FieldDeclaration field = (FieldDeclaration) node.getParent();
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			
			this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		
		} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			// MethodDeclaration method = (MethodDeclaration) node.getParent();
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, });
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		
		} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			
			this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		ASTNode relevantParent = getRelevantParent(node);

		switch (relevantParent.getNodeType()) {
		case ASTNode.FIELD_DECLARATION:
			// FieldDeclaration fd = (FieldDeclaration) relevantParent;
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			
			this.dependenciesCP.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()) });
			break;
		case ASTNode.METHOD_DECLARATION:
			// MethodDeclaration md = (MethodDeclaration) relevantParent;
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			
			this.dependenciesCP.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()) });
			break;
		case ASTNode.INITIALIZER:
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			this.dependenciesCP.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()) });
			break;
		}

		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		Type a = node.getType();
		if(MC) this.dependenciesMC.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE, this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(), lineMethod});
		
		if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
				this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE, this.getTargetClassName(node.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
			}else{
				this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE, this.getTargetClassName(node.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}
			}
		}
		
		this.dependenciesCP.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE, this.getTargetClassName(node.getType().resolveBinding()) });
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if(!blockIndex.equals("")){
			methodInterFirstBlock=true;
			interno=true;
			blocosExternos.add(blockIndex);
			metodoExterno.add(methodName);
		}
		MC=true;
		methodName=node.getName().getFullyQualifiedName();
		lineMethod=fullClass.getLineNumber(node.getStartPosition()) - 1;
		
		for (Object o : node.parameters()) {
			if (o instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
				Type b = svd.getType();
				if(MC) this.dependenciesMC.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER, this.getTargetClassName(svd.getType().resolveBinding()), methodName, getClassName(), lineMethod});
				
				if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
						this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER, this.getTargetClassName(svd.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}else{
						this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER, this.getTargetClassName(svd.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
						}
					}
				}
				
				this.dependenciesCP.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER, this.getTargetClassName(svd.getType().resolveBinding()) });
				if (svd.getType().getNodeType() == Type.PARAMETERIZED_TYPE) {
					// TODO: Adjust the way that we handle parameter types
					for (Object t : ((ParameterizedType) svd.getType()).typeArguments()) {
						if (t instanceof SimpleType) {
							SimpleType st = (SimpleType) t;
							if(MC) this.dependenciesMC.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER, this.getTargetClassName(st.resolveBinding()), methodName, getClassName(), lineMethod});
							
							if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
									this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER, this.getTargetClassName(st.resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
								}else{
									this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER, this.getTargetClassName(st.resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
									}
								}
							}
							
							this.dependenciesCP.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER, this.getTargetClassName(st.resolveBinding()) });
						} else if (t instanceof ParameterizedType) {
							ParameterizedType pt = (ParameterizedType) t;
							if(MC) this.dependenciesMC.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER, this.getTargetClassName(pt.getType().resolveBinding()), methodName, getClassName(), lineMethod});
							
							if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
									this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER, this.getTargetClassName(pt.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
								}else{
									this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER, this.getTargetClassName(pt.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
									}
								}
							}
							
							this.dependenciesCP.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER, this.getTargetClassName(pt.getType().resolveBinding()) });
						}
					}
				}

			}
		}
		for (Object o : node.thrownExceptions()) {
			Name name = (Name) o;
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.THROW, this.getTargetClassName(name.resolveTypeBinding()), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.THROW, this.getTargetClassName(name.resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.THROW, this.getTargetClassName(name.resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			
			this.dependenciesCP.add(new Object[] { DependencyType.THROW, this.getTargetClassName(name.resolveTypeBinding()) });
		}

		if (node.getReturnType2() != null
				&& !(node.getReturnType2().isPrimitiveType() && ((PrimitiveType) node.getReturnType2()).getPrimitiveTypeCode() == PrimitiveType.VOID)) {
			if (!node.getReturnType2().resolveBinding().isTypeVariable()) {
				ASTNode b = node.getParent();
				if(MC) this.dependenciesMC.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(node.getReturnType2().resolveBinding()), methodName, getClassName(), lineMethod});
				
				if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
						this.dependenciesBM.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(node.getReturnType2().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}else{
						this.dependenciesBM.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(node.getReturnType2().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
						}
					}
				}
				
				this.dependenciesCP.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(node.getReturnType2().resolveBinding()) });
			} else {
				if (node.getReturnType2().resolveBinding().getTypeBounds().length >= 1) {
					if(MC) this.dependenciesMC.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(node.getReturnType2().resolveBinding().getTypeBounds()[0]), methodName, getClassName(), lineMethod});
					
					if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
							this.dependenciesBM.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(node.getReturnType2().resolveBinding().getTypeBounds()[0]), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
						}else{
							this.dependenciesBM.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(node.getReturnType2().resolveBinding().getTypeBounds()[0]), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
							}
						}
					}
					
					this.dependenciesCP.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(node.getReturnType2().resolveBinding().getTypeBounds()[0]) });
				}
			}

		}
		
		if(node.getBody()==null){
			MC=false;
		}
		return true;
	}
	
	@Override
	public void endVisit(MethodDeclaration node) {
		if(!blockIndex.equals("")){
			
			ASTNode parent = node.getParent();
			
			while (parent.getNodeType()!=ASTNode.METHOD_DECLARATION && parent.getParent()!=null){
				parent = parent.getParent();
			}
			
			if(parent.getNodeType()==ASTNode.METHOD_DECLARATION){
				interno=false;
				blocosExternos.remove(blocosExternos.size()-1);
				metodoExterno.remove(metodoExterno.size()-1);
				
				MethodDeclaration md = (MethodDeclaration)parent;
				methodName=md.getName().getFullyQualifiedName();
				lineMethod=fullClass.getLineNumber(md.getStartPosition()) - 1;
			}
		}
		else{	
			MC=false;
		}
	}
	
	@Override
	public boolean visit(Block node) {
		count2++;
		if(MC && count2>1 && !methodInterFirstBlock){
			bloco++;
			count3++;
			
			if(blockIndex.equals("")) blockIndex = blockIndex+nextvalue;
			else{
				blockIndex = blockIndex+"-"+nextvalue;
			}
			
			nextvalue=1;
			
			lineBlock = fullClass.getLineNumber(node.getStartPosition()) - 1;
			linelist.add(lineBlock);
			blist.add(nextvalue);
			
			//node.getParent().accept(this);
			
		}
		if(methodInterFirstBlock) {
			methodInterFirstBlock=false;
			count3=0;
		}
		return true;
	}
	
	@Override
	public void endVisit(Block node) {
		count2--;
		if(MC && count2>0 && count3>0){
			if(linelist.size()>1) lineBlock = linelist.get(bloco-2);
			else lineBlock =0;
			
			linelist.remove(bloco-1);
			blist.remove(bloco-1);
			
			if(blockIndex.length()==1){
				nextvalue= Integer.parseInt(blockIndex)+1;
				blockIndex = "";
			}
			else {
				nextvalue= Integer.parseInt(blockIndex.substring(blockIndex.length() - 1))+1;
				blockIndex = blockIndex.substring(0, blockIndex.length()-2);
			}
			
			bloco--;
			count3--;
		}
		else if(count3<=0){
			count3=count2;
		}
	}
	
	
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ASTNode relevantParent = getRelevantParent(node);

		switch (relevantParent.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			// MethodDeclaration md = (MethodDeclaration) relevantParent;
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE, this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE, this.getTargetClassName(node.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE, this.getTargetClassName(node.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			
			this.dependenciesCP.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE, this.getTargetClassName(node.getType().resolveBinding()) });

			break;
		case ASTNode.INITIALIZER:
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE, this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE, this.getTargetClassName(node.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)).substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE, this.getTargetClassName(node.getType().resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)).substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			this.dependenciesCP.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE, this.getTargetClassName(node.getType().resolveBinding()) });
			break;
		}

		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		ASTNode relevantParent = getRelevantParent(node);
		
		// int isStatic;
		//
		// if (node.resolveMethodBinding() != null){
		// isStatic = node.resolveMethodBinding().getModifiers() &
		// Modifier.STATIC;
		// }else{
		// isStatic = false;
		// }

		switch (relevantParent.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			// MethodDeclaration md = (MethodDeclaration) relevantParent;
			if (node.getExpression() != null) {
				if(MC) this.dependenciesMC.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName, getClassName(), lineMethod});
				
				if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}else{
						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
						}
					}
				}
				
				this.dependenciesCP.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()) });
			} else if (node.resolveMethodBinding() != null) {
				if(MC) this.dependenciesMC.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()), methodName, getClassName(), lineMethod});
				
				if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}else{
						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
						}
					}
				}
				
				this.dependenciesCP.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()) });
			}
			break;
		case ASTNode.INITIALIZER:
			if (node.getExpression() != null) {
				if(MC) this.dependenciesMC.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName, getClassName(), lineMethod});
				
				if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}else{
						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
						}
					}
				}
				
				this.dependenciesCP.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()) });
			} else if (node.resolveMethodBinding() != null) {
				if(MC) this.dependenciesMC.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()), methodName, getClassName(), lineMethod});
				
				if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}else{
						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
						}
					}
				}
				
				this.dependenciesCP.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()) });
			}
			break;
		}
		return true;
	}

	@Override
	public boolean visit(FieldAccess node) {
		ASTNode relevantParent = getRelevantParent(node);

		// int isStatic = node.resolveFieldBinding().getModifiers() &
		// Modifier.STATIC;

		switch (relevantParent.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			// MethodDeclaration md = (MethodDeclaration) relevantParent;
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			
			this.dependenciesCP.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()) });
			break;
		case ASTNode.INITIALIZER:
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});}
								
				}
			}
			
			this.dependenciesCP.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()) });
			break;
		}
		return true;
	}

	@Override
	public boolean visit(QualifiedName node) {
		if ((node.getParent().getNodeType() == ASTNode.METHOD_INVOCATION || node.getParent().getNodeType() == ASTNode.INFIX_EXPRESSION
				|| node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT || node.getParent().getNodeType() == ASTNode.ASSIGNMENT)
				&& node.getQualifier().getNodeType() != ASTNode.QUALIFIED_NAME && node.getQualifier().getNodeType() != ASTNode.SIMPLE_NAME) {
			ASTNode relevantParent = getRelevantParent(node);
			// int isStatic = node.resolveBinding().getModifiers() &
			// Modifier.STATIC;

			switch (relevantParent.getNodeType()) {
			case ASTNode.METHOD_DECLARATION:
				// MethodDeclaration md = (MethodDeclaration) relevantParent;
				if(MC) this.dependenciesMC.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getQualifier().resolveTypeBinding()), methodName, getClassName(), lineMethod});
				
				if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getQualifier().resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}else{
						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getQualifier().resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
						}
					}
				}
				
				this.dependenciesCP.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getQualifier().resolveTypeBinding()) });
				break;
			case ASTNode.INITIALIZER:
				if(MC) this.dependenciesMC.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getQualifier().resolveTypeBinding()), methodName, getClassName(), lineMethod});
				
				if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getQualifier().resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}else{
						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getQualifier().resolveTypeBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
						}
					}
				}
				
				this.dependenciesCP.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getQualifier().resolveTypeBinding()) });
				break;
			}

		}
		
		

		return true;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return super.visit(node);
	}

	public boolean visit(org.eclipse.jdt.core.dom.NormalAnnotation node) {
		if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
			// FieldDeclaration field = (FieldDeclaration) node.getParent();
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
					}
				}
			}
			
			this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			// MethodDeclaration method = (MethodDeclaration) node.getParent();
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}else{
					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
				}
				}
			}
			
			this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			if(MC) this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(), lineMethod});
			
			if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
									this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
								} else{
									this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName(), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
								}
				}
			}
			
			
			
			this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		}
		return true;
	};

	@Override
	public boolean visit(ParameterizedType node) {
		ASTNode relevantParent = this.getRelevantParent(node);
		if (node.getNodeType() == ASTNode.PARAMETERIZED_TYPE) {
			ParameterizedType pt = (ParameterizedType) node;
			if (pt.typeArguments() != null) {
				for (Object o : pt.typeArguments()) {
					Type t = (Type) o;
					if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
						// MethodDeclaration md = (MethodDeclaration)
						// relevantParent;
						//TODO: esse if else nao serve pra nada
						if(MC) this.dependenciesMC.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(t.resolveBinding()), methodName, getClassName(), lineMethod});
						
						if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
									this.dependenciesBM.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(t.resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
								}else{
									this.dependenciesBM.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(t.resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
								}
								
								
							}
						}
						this.dependenciesCP.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(t.resolveBinding()) });
					} else {
						if(MC) this.dependenciesMC.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(t.resolveBinding()), methodName, getClassName(), lineMethod});
						
						if(MC && bloco>0){ 
							String metName=methodName;
							for(int i=0; i<blist.size(); i++){
								
								if(interno){
									if(blocosExternos.contains(blockIndex.substring(0, blockIndex.length()-(i*2)))){
										int pos =-1;
										for(int j=0; j<blocosExternos.size(); j++){
											if(blocosExternos.get(j).equals(blockIndex.substring(0, blockIndex.length()-(i*2)))){
												pos = j;
											}
										}
										metName=metodoExterno.get(pos);
									}
									this.dependenciesBM.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(t.resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
								}else{
									this.dependenciesBM.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(t.resolveBinding()), metName, blockIndex.substring(0, blockIndex.length()-(i*2)), getClassName(), lineBlock});
								}
								
								
							}
						}
						this.dependenciesCP.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(t.resolveBinding()) });
					}
				}
			}
		}
		return true;
	}

	private ASTNode getRelevantParent(final ASTNode node) {
		for (ASTNode aux = node; aux != null; aux = aux.getParent()) {
			switch (aux.getNodeType()) {
			case ASTNode.FIELD_DECLARATION:
			case ASTNode.METHOD_DECLARATION:
			case ASTNode.INITIALIZER:
				return aux;
			}
		}
		return node;
	}

	private String getTargetClassName(ITypeBinding type) {
		String result = "";
		try {

			if (!type.isAnonymous() && type.getQualifiedName() != null && !type.getQualifiedName().isEmpty()) {
				result = type.getQualifiedName();
			} else if (type.isLocal() && type.getName() != null && !type.getName().isEmpty()) {
				result = type.getName();
			} else if (type.getSuperclass() != null && !type.getSuperclass().getQualifiedName().equals("java.lang.Object")
					&& (type.getInterfaces() == null || type.getInterfaces().length == 0)) {
				result = type.getSuperclass().getQualifiedName();
			} else if (type.getInterfaces() != null && type.getInterfaces().length == 1) {
				result = type.getInterfaces()[0].getQualifiedName();
			} else if (type.isArray() && type.getElementType() != null && type.getElementType().isLocal()) {
				result = type.getElementType().getName();
			} else if (type.isFromSource() && type.getSuperclass() != null) {
				result = type.getSuperclass().getQualifiedName();
			}

			if (result.equals("")) {
				// result = "unknown";
				throw new RuntimeException("AST Parser error.");
			} else if (result.endsWith("[]")) {
				result = result.substring(0, result.length() - 2);
			} else if (result.matches(".*<.*>")) {
				result = result.replaceAll("<.*>", "");
			}
		} catch (Throwable t) {
			System.out.println(t);
		}
		return result;
	}

}
