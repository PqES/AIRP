package airptool.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
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
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;

import airptool.enums.DependencyType;

public class DependencyVisitor extends ASTVisitor {
	private List<Object[]> dependenciesCP;
	private List<Object[]> dependenciesMC;
	private List<Object[]> dependenciesBM;

	private ICompilationUnit unit;
	private CompilationUnit fullClass;
	private String className;
	private boolean MC, interno, isInterface, isGetterOrSetter, isVisitingParent, visitParent;
	private int count;
	private int bloco;
	private ArrayList<Integer> linelist;
	private ArrayList<Integer> blist;
	private int nextvalue, lineMethod, lineBlock;
	private String methodName;
	private String blockIndex;

	public DependencyVisitor(ICompilationUnit unit) throws JavaModelException {
		if (!isInterface)
			this.dependenciesCP = new ArrayList<Object[]>();
		this.dependenciesMC = new ArrayList<Object[]>();
		this.dependenciesBM = new ArrayList<Object[]>();
		this.unit = unit;
		this.MC = false;
		this.isInterface = false;
		this.isGetterOrSetter = false;
		this.interno = false;
		this.isVisitingParent = false;
		this.visitParent = false;
		this.count = 0;
		this.bloco = 0;
		this.blist = new ArrayList<Integer>();
		this.linelist = new ArrayList<Integer>();
		this.lineMethod = 0;
		this.lineBlock = 0;
		this.nextvalue = 1;
		this.methodName = "";
		this.blockIndex = "";

		this.className = unit.getParent().getElementName() + "."
				+ unit.getElementName().substring(0, unit.getElementName().length() - 5);
		ASTParser parser = ASTParser.newParser(AST.JLS4); // It was JSL3, but it
															// is now deprecated
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);

		this.fullClass = (CompilationUnit) parser.createAST(null);// parse
		this.fullClass.accept(this);

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
		if (!isVisitingParent) {
			if (!node.isLocalTypeDeclaration() && !node.isMemberTypeDeclaration()) { // Para
																						// evitar
																						// fazer
																						// v�rias
																						// vezes
				try {
					IType type = (IType) unit.getTypes()[0];

					if (type.isInterface())
						isInterface = true;

					ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(null);

					IType[] typeSuperclasses = typeHierarchy.getAllSuperclasses(type);

					for (IType t : typeSuperclasses) {
						if (node.getSuperclassType() != null && t.getFullyQualifiedName()
								.equals(node.getSuperclassType().resolveBinding().getQualifiedName())) {
							if (MC && !isInterface && !isGetterOrSetter)
								this.dependenciesMC.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName(),
										methodName, getClassName(), lineMethod,
										fullClass.getLineNumber(node.getSuperclassType().getStartPosition()),
										node.getSuperclassType().getStartPosition(),
										node.getSuperclassType().getLength() });

							if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

								this.dependenciesBM.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName(),
										methodName, blockIndex, getClassName(), lineBlock,
										fullClass.getLineNumber(node.getSuperclassType().getStartPosition()),
										node.getSuperclassType().getStartPosition(),
										node.getSuperclassType().getLength() });

								String blockIndexTemp = blockIndex;

								for (int i = 1; i < blist.size(); i++) {

									blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

									this.dependenciesBM
											.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName(),
													methodName, blockIndexTemp, getClassName(), lineBlock,
													fullClass
															.getLineNumber(node.getSuperclassType().getStartPosition()),
													node.getSuperclassType().getStartPosition(),
													node.getSuperclassType().getLength() });
								}

							}
							if (!isInterface)
								this.dependenciesCP
										.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName() });
						} else {
							// this.dependencies.add(new Object[] {
							// DependencyType.EXTENDINDIRECT,
							// t.getFullyQualifiedName() });
						}
					}

					IType[] typeSuperinter = typeHierarchy.getAllInterfaces();

					externo: for (IType t : typeSuperinter) {
						for (Object it : node.superInterfaceTypes()) {
							switch (((Type) it).getNodeType()) {
							case ASTNode.SIMPLE_TYPE:
								SimpleType st = (SimpleType) it;
								if (t.getFullyQualifiedName()
										.equals(st.getName().resolveTypeBinding().getQualifiedName())) {
									if (!type.isInterface()) {
										if (MC && !isInterface && !isGetterOrSetter)
											this.dependenciesMC.add(new Object[] { DependencyType.IMPLEMENT,
													t.getFullyQualifiedName(), methodName, getClassName(), lineMethod,
													fullClass.getLineNumber(st.getStartPosition()),
													st.getStartPosition(), st.getLength() });

										if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

											this.dependenciesBM.add(new Object[] { DependencyType.IMPLEMENT,
													t.getFullyQualifiedName(), methodName, blockIndex, getClassName(),
													lineBlock, fullClass.getLineNumber(st.getStartPosition()),
													st.getStartPosition(), st.getLength() });

											String blockIndexTemp = blockIndex;

											for (int i = 1; i < blist.size(); i++) {

												blockIndexTemp = blockIndexTemp.substring(0,
														blockIndexTemp.lastIndexOf("-"));

												this.dependenciesBM.add(new Object[] { DependencyType.IMPLEMENT,
														t.getFullyQualifiedName(), methodName, blockIndexTemp,
														getClassName(), lineBlock,
														fullClass.getLineNumber(st.getStartPosition()),
														st.getStartPosition(), st.getLength() });
											}

										}

										if (!isInterface)
											this.dependenciesCP.add(new Object[] { DependencyType.IMPLEMENT,
													t.getFullyQualifiedName() });
									} else {
										if (MC && !isInterface && !isGetterOrSetter)
											this.dependenciesMC.add(new Object[] { DependencyType.EXTEND,
													t.getFullyQualifiedName(), methodName, getClassName(), lineMethod,
													fullClass.getLineNumber(st.getStartPosition()),
													st.getStartPosition(), st.getLength() });

										if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

											this.dependenciesBM.add(new Object[] { DependencyType.EXTEND,
													t.getFullyQualifiedName(), methodName, blockIndex, getClassName(),
													lineBlock, fullClass.getLineNumber(st.getStartPosition()),
													st.getStartPosition(), st.getLength() });

											String blockIndexTemp = blockIndex;

											for (int i = 1; i < blist.size(); i++) {

												blockIndexTemp = blockIndexTemp.substring(0,
														blockIndexTemp.lastIndexOf("-"));

												this.dependenciesBM.add(
														new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName(),
																methodName, blockIndexTemp, getClassName(), lineBlock,
																fullClass.getLineNumber(st.getStartPosition()),
																st.getStartPosition(), st.getLength() });
											}
										}

										if (!isInterface)
											this.dependenciesCP.add(
													new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName() });
									}
									continue externo;
								}
								break;
							case ASTNode.PARAMETERIZED_TYPE:
								ParameterizedType pt = (ParameterizedType) it;
								if (t != null && t.getFullyQualifiedName() != null && pt != null && pt.getType() != null
										&& pt.getType().resolveBinding() != null && t.getFullyQualifiedName()
												.equals(pt.getType().resolveBinding().getBinaryName())) {
									if (!type.isInterface()) {
										if (MC && !isInterface && !isGetterOrSetter)
											this.dependenciesMC.add(new Object[] { DependencyType.IMPLEMENT,
													t.getFullyQualifiedName(), methodName, getClassName(), lineMethod,
													fullClass.getLineNumber(pt.getStartPosition()),
													pt.getStartPosition(), pt.getLength() });

										if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

											this.dependenciesBM.add(new Object[] { DependencyType.IMPLEMENT,
													t.getFullyQualifiedName(), methodName, blockIndex, getClassName(),
													lineBlock, fullClass.getLineNumber(pt.getStartPosition()),
													pt.getStartPosition(), pt.getLength() });

											String blockIndexTemp = blockIndex;

											for (int i = 1; i < blist.size(); i++) {

												blockIndexTemp = blockIndexTemp.substring(0,
														blockIndexTemp.lastIndexOf("-"));

												this.dependenciesBM.add(new Object[] { DependencyType.IMPLEMENT,
														t.getFullyQualifiedName(), methodName, blockIndexTemp,
														getClassName(), lineBlock,
														fullClass.getLineNumber(pt.getStartPosition()),
														pt.getStartPosition(), pt.getLength() });
											}

										}

										if (!isInterface)
											this.dependenciesCP.add(new Object[] { DependencyType.IMPLEMENT,
													t.getFullyQualifiedName() });
									} else {
										if (MC && !isInterface && !isGetterOrSetter)
											this.dependenciesMC.add(new Object[] { DependencyType.EXTEND,
													t.getFullyQualifiedName(), methodName, getClassName(), lineMethod,
													fullClass.getLineNumber(pt.getStartPosition()),
													pt.getStartPosition(), pt.getLength() });

										if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

											this.dependenciesBM.add(new Object[] { DependencyType.EXTEND,
													t.getFullyQualifiedName(), methodName, blockIndex, getClassName(),
													lineBlock, fullClass.getLineNumber(pt.getStartPosition()),
													pt.getStartPosition(), pt.getLength() });

											String blockIndexTemp = blockIndex;

											for (int i = 1; i < blist.size(); i++) {

												blockIndexTemp = blockIndexTemp.substring(0,
														blockIndexTemp.lastIndexOf("-"));

												this.dependenciesBM.add(
														new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName(),
																methodName, blockIndexTemp, getClassName(), lineBlock,
																fullClass.getLineNumber(pt.getStartPosition()),
																pt.getStartPosition(), pt.getLength() });
											}

										}
										if (!isInterface)
											this.dependenciesCP.add(
													new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName() });
									}
									continue externo;
								}
								break;
							}
						}
						// this.dependencies.add(new Object[] {
						// DependencyType.IMPLEMENTINDIRECT,
						// t.getFullyQualifiedName() });
					}
				} catch (JavaModelException e) {
					throw new RuntimeException("AST Parser error.", e);
				}
			}
		}
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		if (!isVisitingParent) {
			if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
				// FieldDeclaration field = (FieldDeclaration) node.getParent();
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
							node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });
					}

				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName() });

			} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
				// MethodDeclaration method = (MethodDeclaration)
				// node.getParent();
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
							node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });
					}
				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName() });

			} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
							node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });
					}

				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName() });

			} else if (node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
				// VariableDeclarationStatement st =
				// (VariableDeclarationStatement)
				// node.getParent();
				// VariableDeclarationFragment vdf =
				// ((VariableDeclarationFragment)
				// st.fragments().get(0));
				ASTNode relevantParent = this.getRelevantParent(node);
				if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
					// MethodDeclaration md = (MethodDeclaration)
					// relevantParent;
					if (MC && !isInterface && !isGetterOrSetter)
						this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(),
								lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
								node.getLength() });

					if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndex,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

						String blockIndexTemp = blockIndex;

						for (int i = 1; i < blist.size(); i++) {

							blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

							this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
									node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName,
									blockIndexTemp, getClassName(), lineBlock,
									fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
									node.getLength() });
						}

					}

					if (!isInterface)
						this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName() });
				}
			} else if (node.getParent().getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
				// SingleVariableDeclaration sv = (SingleVariableDeclaration)
				// node.getParent();
				ASTNode relevantParent = this.getRelevantParent(node);
				if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
					// MethodDeclaration md = (MethodDeclaration)
					// relevantParent;
					if (MC && !isInterface && !isGetterOrSetter)
						this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(),
								lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
								node.getLength() });

					if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndex,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

						String blockIndexTemp = blockIndex;

						for (int i = 1; i < blist.size(); i++) {

							blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

							this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
									node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName,
									blockIndexTemp, getClassName(), lineBlock,
									fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
									node.getLength() });
						}

					}

					if (!isInterface)
						this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName() });
				}

			}
		}
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		if (!isVisitingParent) {
			if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
				// FieldDeclaration field = (FieldDeclaration) node.getParent();
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
							node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });
					}

				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName() });

			} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
				// MethodDeclaration method = (MethodDeclaration)
				// node.getParent();
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
							node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });
					}

				}
				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName() });

			} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
							node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });
					}
				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName() });
			}
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		// TODO: arrumar negócio de detectar classe interna
		if (!isVisitingParent) {
			ASTNode relevantParent = getRelevantParent(node);

			switch (relevantParent.getNodeType()) {
			case ASTNode.FIELD_DECLARATION:
				// FieldDeclaration fd = (FieldDeclaration) relevantParent;
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.CREATE,
							this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
							node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.CREATE,
							this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.CREATE,
								this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });
					}

				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.CREATE,
							this.getTargetClassName(node.getType().resolveBinding()) });
				break;
			case ASTNode.METHOD_DECLARATION:
				// MethodDeclaration md = (MethodDeclaration) relevantParent;
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.CREATE,
							this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
							node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.CREATE,
							this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.CREATE,
								this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });
					}
				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.CREATE,
							this.getTargetClassName(node.getType().resolveBinding()) });
				break;
			case ASTNode.INITIALIZER:
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.CREATE,
							this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
							node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.CREATE,
							this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.CREATE,
								this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });
					}

				}
				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.CREATE,
							this.getTargetClassName(node.getType().resolveBinding()) });
				break;
			}

		}
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		if (!isVisitingParent) {
			if (MC && !isInterface && !isGetterOrSetter)
				this.dependenciesMC.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE,
						this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(),
						lineMethod, fullClass.getLineNumber(node.getType().getStartPosition()),
						node.getType().getStartPosition(), node.getType().getLength() });

			if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

				this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE,
						this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndex,
						getClassName(), lineBlock, fullClass.getLineNumber(node.getType().getStartPosition()),
						node.getType().getStartPosition(), node.getType().getLength() });

				String blockIndexTemp = blockIndex;

				for (int i = 1; i < blist.size(); i++) {

					blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

					this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE,
							this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndexTemp,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getType().getStartPosition()),
							node.getType().getStartPosition(), node.getType().getLength() });
				}

			}

			if (!isInterface)
				this.dependenciesCP.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE,
						this.getTargetClassName(node.getType().resolveBinding()) });
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (!isVisitingParent) {
			if (Modifier.isPublic(node.getModifiers()) && node.typeParameters().size() == 0) {
				if (node.getName().getFullyQualifiedName().matches("^get[A-Z].*") && (node.getReturnType2() != null
						&& !(node.getReturnType2().isPrimitiveType() && ((PrimitiveType) node.getReturnType2())
								.getPrimitiveTypeCode() == PrimitiveType.VOID)))
					isGetterOrSetter = true;
			}

			if (Modifier.isPublic(node.getModifiers())
					&& (node.getReturnType2() != null && (node.getReturnType2().isPrimitiveType()
							&& ((PrimitiveType) node.getReturnType2()).getPrimitiveTypeCode() == PrimitiveType.VOID))
					/*&& node.parameters().size() == 1*/
					&& node.getName().getFullyQualifiedName().matches("^set[A-Z].*"))
				isGetterOrSetter = true;

			if (MC && !isInterface && !isGetterOrSetter) {
				interno = true;

			} else {
				methodName = node.getName().getFullyQualifiedName();
				lineMethod = fullClass.getLineNumber(node.getStartPosition()) - 1;
			}
			MC = true;

			for (Object o : node.parameters()) {
				if (o instanceof SingleVariableDeclaration) {
					SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
					if (MC && !isInterface && !isGetterOrSetter)
						this.dependenciesMC.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER,
								this.getTargetClassName(svd.getType().resolveBinding()), methodName, getClassName(),
								lineMethod, fullClass.getLineNumber(svd.getStartPosition()), svd.getStartPosition(),
								svd.getLength() });

					if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

						this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER,
								this.getTargetClassName(svd.getType().resolveBinding()), methodName, blockIndex,
								getClassName(), lineBlock, fullClass.getLineNumber(svd.getStartPosition()),
								svd.getStartPosition(), svd.getLength() });

						String blockIndexTemp = blockIndex;

						for (int i = 1; i < blist.size(); i++) {

							blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

							this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER,
									this.getTargetClassName(svd.getType().resolveBinding()), methodName, blockIndexTemp,
									getClassName(), lineBlock, fullClass.getLineNumber(svd.getStartPosition()),
									svd.getStartPosition(), svd.getLength() });
						}
					}

					if (!isInterface)
						this.dependenciesCP.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER,
								this.getTargetClassName(svd.getType().resolveBinding()) });

					if (svd.getType().getNodeType() == Type.PARAMETERIZED_TYPE) {
						// TODO: Adjust the way that we handle parameter types
						for (Object t : ((ParameterizedType) svd.getType()).typeArguments()) {
							if (t instanceof SimpleType) {
								SimpleType st = (SimpleType) t;
								if (MC && !isInterface && !isGetterOrSetter)
									this.dependenciesMC.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER,
											this.getTargetClassName(st.resolveBinding()), methodName, getClassName(),
											lineMethod, fullClass.getLineNumber(st.getStartPosition()),
											st.getStartPosition(), st.getLength() });

								if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

									this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER,
											this.getTargetClassName(st.resolveBinding()), methodName, blockIndex,
											getClassName(), lineBlock, fullClass.getLineNumber(st.getStartPosition()),
											st.getStartPosition(), st.getLength() });

									String blockIndexTemp = blockIndex;

									for (int i = 1; i < blist.size(); i++) {

										blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

										this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER,
												this.getTargetClassName(st.resolveBinding()), methodName,
												blockIndexTemp, getClassName(), lineBlock,
												fullClass.getLineNumber(st.getStartPosition()), st.getStartPosition(),
												st.getLength() });
									}

								}

								if (!isInterface)
									this.dependenciesCP.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER,
											this.getTargetClassName(st.resolveBinding()) });

							} else if (t instanceof ParameterizedType) {
								ParameterizedType pt = (ParameterizedType) t;
								if (MC && !isInterface && !isGetterOrSetter)
									this.dependenciesMC.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER,
											this.getTargetClassName(pt.getType().resolveBinding()), methodName,
											getClassName(), lineMethod, fullClass.getLineNumber(pt.getStartPosition()),
											pt.getStartPosition(), pt.getLength() });

								if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

									this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER,
											this.getTargetClassName(pt.getType().resolveBinding()), methodName,
											blockIndex, getClassName(), lineBlock,
											fullClass.getLineNumber(pt.getStartPosition()), pt.getStartPosition(),
											pt.getLength() });

									String blockIndexTemp = blockIndex;

									for (int i = 1; i < blist.size(); i++) {

										blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

										this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER,
												this.getTargetClassName(pt.getType().resolveBinding()), methodName,
												blockIndexTemp, getClassName(), lineBlock,
												fullClass.getLineNumber(pt.getStartPosition()), pt.getStartPosition(),
												pt.getLength() });
									}

								}

								if (!isInterface)
									this.dependenciesCP.add(new Object[] { DependencyType.DECLARE_FORMAL_PARAMETER,
											this.getTargetClassName(pt.getType().resolveBinding()) });
							}
						}
					}

				}
			}
			for (Object o : node.thrownExceptions()) {
				Name name = (Name) o;
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.THROW,
							this.getTargetClassName(name.resolveTypeBinding()), methodName, getClassName(), lineMethod,
							fullClass.getLineNumber(name.getStartPosition()), name.getStartPosition(),
							name.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.THROW,
							this.getTargetClassName(name.resolveTypeBinding()), methodName, blockIndex, getClassName(),
							lineBlock, fullClass.getLineNumber(name.getStartPosition()), name.getStartPosition(),
							name.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.THROW,
								this.getTargetClassName(name.resolveTypeBinding()), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(name.getStartPosition()),
								name.getStartPosition(), name.getLength() });
					}
				}

				if (!isInterface)
					this.dependenciesCP.add(
							new Object[] { DependencyType.THROW, this.getTargetClassName(name.resolveTypeBinding()) });
			}

			if (node.getReturnType2() != null && !(node.getReturnType2().isPrimitiveType()
					&& ((PrimitiveType) node.getReturnType2()).getPrimitiveTypeCode() == PrimitiveType.VOID)) {
				if (!node.getReturnType2().resolveBinding().isTypeVariable()) {
					ASTNode b = node.getParent();
					if (MC && !isInterface && !isGetterOrSetter)
						this.dependenciesMC.add(new Object[] { DependencyType.DECLARE,
								this.getTargetClassName(node.getReturnType2().resolveBinding()), methodName,
								getClassName(), lineMethod,
								fullClass.getLineNumber(node.getReturnType2().getStartPosition()),
								node.getReturnType2().getStartPosition(), node.getReturnType2().getLength() });

					if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

						this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
								this.getTargetClassName(node.getReturnType2().resolveBinding()), methodName, blockIndex,
								getClassName(), lineBlock,
								fullClass.getLineNumber(node.getReturnType2().getStartPosition()),
								node.getReturnType2().getStartPosition(), node.getReturnType2().getLength() });

						String blockIndexTemp = blockIndex;

						for (int i = 1; i < blist.size(); i++) {

							blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

							this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
									this.getTargetClassName(node.getReturnType2().resolveBinding()), methodName,
									blockIndexTemp, getClassName(), lineBlock,
									fullClass.getLineNumber(node.getReturnType2().getStartPosition()),
									node.getReturnType2().getStartPosition(), node.getReturnType2().getLength() });
						}
					}

					if (!isInterface)
						this.dependenciesCP.add(new Object[] { DependencyType.DECLARE,
								this.getTargetClassName(node.getReturnType2().resolveBinding()) });
				} else {
					if (node.getReturnType2().resolveBinding().getTypeBounds().length >= 1) {
						if (MC && !isInterface && !isGetterOrSetter)
							this.dependenciesMC.add(new Object[] { DependencyType.DECLARE,
									this.getTargetClassName(node.getReturnType2().resolveBinding().getTypeBounds()[0]),
									methodName, getClassName(), lineMethod,
									fullClass.getLineNumber(node.getReturnType2().getStartPosition()),
									node.getReturnType2().getStartPosition(), node.getReturnType2().getLength() });

						if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

							this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
									this.getTargetClassName(node.getReturnType2().resolveBinding().getTypeBounds()[0]),
									methodName, blockIndex, getClassName(), lineBlock,
									fullClass.getLineNumber(node.getReturnType2().getStartPosition()),
									node.getReturnType2().getStartPosition(), node.getReturnType2().getLength() });

							String blockIndexTemp = blockIndex;

							for (int i = 1; i < blist.size(); i++) {

								blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

								this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
										this.getTargetClassName(
												node.getReturnType2().resolveBinding().getTypeBounds()[0]),
										methodName, blockIndexTemp, getClassName(), lineBlock,
										fullClass.getLineNumber(node.getReturnType2().getStartPosition()),
										node.getReturnType2().getStartPosition(), node.getReturnType2().getLength() });
							}
						}

						if (!isInterface)
							this.dependenciesCP.add(new Object[] { DependencyType.DECLARE, this
									.getTargetClassName(node.getReturnType2().resolveBinding().getTypeBounds()[0]) });
					}
				}

			}

			if (node.getBody() == null) {
				MC = false;
			}
		}
		return true;
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		if (!isVisitingParent) {
			if (interno) {

				ASTNode parent = node.getParent();

				while (parent.getNodeType() != ASTNode.METHOD_DECLARATION && parent.getParent() != null) {
					parent = parent.getParent();
				}

				if (parent.getNodeType() == ASTNode.METHOD_DECLARATION) {
					interno = false;

					MethodDeclaration md = (MethodDeclaration) parent;
					methodName = md.getName().getFullyQualifiedName();
					lineMethod = fullClass.getLineNumber(md.getStartPosition()) - 1;
				}
			} else {
				MC = false;
			}

			isGetterOrSetter = false;
		}
	}

	@Override
	public boolean visit(Block node) {
		// TODO: Talvez usar um Exception ao inves do boolean isVisitingParent
		// pra evitar de pegar as dependências 2 vezes
		// TODO: Verificar o pai de um ELSE, mas 99% de certeza que é um IF
		// mesmo, etão tudo bem.
		// TODO: Fazer um Visit para o DO-WHILE pois a dependência do DO só vai
		// estar no final e não vai pegar. TRY-CATCH a mesma coisa.
		if (!isVisitingParent) {

			if (!visitParent) {

				count++;
				if (MC && count > 0) {
					bloco++;

					if (blockIndex.equals(""))
						blockIndex = blockIndex + nextvalue;
					else {
						blockIndex = blockIndex + "-" + nextvalue;
					}

					nextvalue = 1;

					lineBlock = fullClass.getLineNumber(node.getStartPosition()) - 1;
					linelist.add(lineBlock);
					blist.add(nextvalue);

				}

				if(node.getParent().getNodeType()!=ASTNode.METHOD_DECLARATION){
					visitParent = true;
					node.getParent().accept(this);
					visitParent = false;
					isVisitingParent = false;
				}
			} else {
				isVisitingParent = true;
			}

		}
		return true;
	}

	@Override
	public void endVisit(Block node) {
		if (!isVisitingParent) {
			count--;
			if (MC && count >= 0) {
				if (linelist.size() > 1)
					lineBlock = linelist.get(bloco - 2);
				else
					lineBlock = 0;

				linelist.remove(bloco - 1);
				blist.remove(bloco - 1);

				if (!blockIndex.contains("-")) {
					nextvalue = Integer.parseInt(blockIndex) + 1;
					blockIndex = "";
				} else {
					nextvalue = Integer.parseInt(blockIndex.substring(blockIndex.lastIndexOf("-") + 1)) + 1;
					blockIndex = blockIndex.substring(0, blockIndex.lastIndexOf("-"));
				}

				bloco--;
			}
		}
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		// TODO: mesma coisa de identificar classe interna
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		if (!isVisitingParent) {
			ASTNode relevantParent = getRelevantParent(node);

			switch (relevantParent.getNodeType()) {
			case ASTNode.METHOD_DECLARATION:
				// MethodDeclaration md = (MethodDeclaration) relevantParent;
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE,
							this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()),
							node.getType().getStartPosition(), node.getType().getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE,
							this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getType().getStartPosition(), node.getType().getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE,
								this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getType().getStartPosition(), node.getType().getLength() });
					}
				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE,
							this.getTargetClassName(node.getType().resolveBinding()) });

				break;
			case ASTNode.INITIALIZER:
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE,
							this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()),
							node.getType().getStartPosition(), node.getType().getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE,
							this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getType().getStartPosition(), node.getType().getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE,
								this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getType().getStartPosition(), node.getType().getLength() });
					}
				}
				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.DECLARE_ATTRIBUTE,
							this.getTargetClassName(node.getType().resolveBinding()) });
				break;
			}

		}
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		if (!isVisitingParent) {
			if (MC && !isInterface && !isGetterOrSetter)
				this.dependenciesMC.add(new Object[] { DependencyType.DECLARE,
						this.getTargetClassName(node.getType().resolveBinding()), methodName, getClassName(),
						lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getType().getStartPosition(),
						node.getType().getLength() });

			if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

				this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
						this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndex,
						getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
						node.getType().getStartPosition(), node.getType().getLength() });

				String blockIndexTemp = blockIndex;

				for (int i = 1; i < blist.size(); i++) {

					blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

					this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
							this.getTargetClassName(node.getType().resolveBinding()), methodName, blockIndexTemp,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getType().getStartPosition(), node.getType().getLength() });
				}
			}
			if (!isInterface)
				this.dependenciesCP.add(new Object[] { DependencyType.DECLARE,
						this.getTargetClassName(node.getType().resolveBinding()) });
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (!isVisitingParent) {
			ASTNode relevantParent = getRelevantParent(node);

			/*
			 * int isStatic; // // if (node.resolveMethodBinding() != null){ //
			 * isStatic = node.resolveMethodBinding().getModifiers() & //
			 * Modifier.STATIC; // }else{ // isStatic = false; }
			 */

			switch (relevantParent.getNodeType()) {
			case ASTNode.METHOD_DECLARATION:
				// MethodDeclaration md = (MethodDeclaration) relevantParent;
				if (node.getExpression() != null) {
					if (MC && !isInterface && !isGetterOrSetter)
						this.dependenciesMC.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName,
								getClassName(), lineMethod, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

					if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName,
								blockIndex, getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

						String blockIndexTemp = blockIndex;

						for (int i = 1; i < blist.size(); i++) {

							blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

							this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
									this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName,
									blockIndexTemp, getClassName(), lineBlock,
									fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
									node.getLength() });
						}
					}

					if (!isInterface)
						this.dependenciesCP.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getExpression().resolveTypeBinding()) });
				} else if (node.resolveMethodBinding() != null) {
					if (MC && !isInterface && !isGetterOrSetter)
						this.dependenciesMC.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()), methodName,
								getClassName(), lineMethod, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

					if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()), methodName,
								blockIndex, getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

						String blockIndexTemp = blockIndex;

						for (int i = 1; i < blist.size(); i++) {

							blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

							this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
									this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()),
									methodName, blockIndexTemp, getClassName(), lineBlock,
									fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
									node.getLength() });
						}
					}

					if (!isInterface)
						this.dependenciesCP.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()) });
				}
				break;
			case ASTNode.INITIALIZER:
				if (node.getExpression() != null) {
					if (MC && !isInterface && !isGetterOrSetter)
						this.dependenciesMC.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName,
								getClassName(), lineMethod, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

					if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName,
								blockIndex, getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

						String blockIndexTemp = blockIndex;

						for (int i = 1; i < blist.size(); i++) {

							blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

							this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
									this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName,
									blockIndexTemp, getClassName(), lineBlock,
									fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
									node.getLength() });
						}
					}

					if (!isInterface)
						this.dependenciesCP.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getExpression().resolveTypeBinding()) });
				} else if (node.resolveMethodBinding() != null) {
					if (MC && !isInterface && !isGetterOrSetter)
						this.dependenciesMC.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()), methodName,
								getClassName(), lineMethod, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

					if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()), methodName,
								blockIndex, getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

						String blockIndexTemp = blockIndex;

						for (int i = 1; i < blist.size(); i++) {

							blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

							this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
									this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()),
									methodName, blockIndexTemp, getClassName(), lineBlock,
									fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
									node.getLength() });
						}
					}

					if (!isInterface)
						this.dependenciesCP.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()) });
				}
				break;
			}
		}
		return true;
	}

	@Override
	public boolean visit(FieldAccess node) {
		if (!isVisitingParent) {
			ASTNode relevantParent = getRelevantParent(node);

			// int isStatic = node.resolveFieldBinding().getModifiers() &
			// Modifier.STATIC;

			switch (relevantParent.getNodeType()) {
			case ASTNode.METHOD_DECLARATION:
				// MethodDeclaration md = (MethodDeclaration) relevantParent;
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.ACCESS,
							this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName,
							getClassName(), lineMethod, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
							this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName,
								blockIndexTemp, getClassName(), lineBlock,
								fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
								node.getLength() });
					}
				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.ACCESS,
							this.getTargetClassName(node.getExpression().resolveTypeBinding()) });
				break;
			case ASTNode.INITIALIZER:
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.ACCESS,
							this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName,
							getClassName(), lineMethod, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
							this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getExpression().resolveTypeBinding()), methodName,
								blockIndexTemp, getClassName(), lineBlock,
								fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
								node.getLength() });
					}
				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.ACCESS,
							this.getTargetClassName(node.getExpression().resolveTypeBinding()) });
				break;
			}
		}
		return true;
	}

	@Override
	public boolean visit(QualifiedName node) {
		if (!isVisitingParent) {
			if ((node.getParent().getNodeType() == ASTNode.METHOD_INVOCATION
					|| node.getParent().getNodeType() == ASTNode.INFIX_EXPRESSION
					|| node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT
					|| node.getParent().getNodeType() == ASTNode.ASSIGNMENT)
					// && node.getQualifier().getNodeType() !=
					// ASTNode.QUALIFIED_NAME &&
					// node.getQualifier().getNodeType()
					// != ASTNode.SIMPLE_NAME) {
					&& node.getQualifier().getNodeType() != ASTNode.QUALIFIED_NAME) {
				ASTNode relevantParent = getRelevantParent(node);
				// int isStatic = node.resolveBinding().getModifiers() &
				// Modifier.STATIC;

				switch (relevantParent.getNodeType()) {
				case ASTNode.METHOD_DECLARATION:
					// MethodDeclaration md = (MethodDeclaration)
					// relevantParent;
					if (MC && !isInterface && !isGetterOrSetter)
						this.dependenciesMC.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getQualifier().resolveTypeBinding()), methodName,
								getClassName(), lineMethod, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

					if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getQualifier().resolveTypeBinding()), methodName,
								blockIndex, getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

						String blockIndexTemp = blockIndex;

						for (int i = 1; i < blist.size(); i++) {

							blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

							this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
									this.getTargetClassName(node.getQualifier().resolveTypeBinding()), methodName,
									blockIndexTemp, getClassName(), lineBlock,
									fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
									node.getLength() });
						}
					}

					if (!isInterface)
						this.dependenciesCP.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getQualifier().resolveTypeBinding()) });
					break;
				case ASTNode.INITIALIZER:
					if (MC && !isInterface && !isGetterOrSetter)
						this.dependenciesMC.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getQualifier().resolveTypeBinding()), methodName,
								getClassName(), lineMethod, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

					if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

						this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getQualifier().resolveTypeBinding()), methodName,
								blockIndex, getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });

						String blockIndexTemp = blockIndex;

						for (int i = 1; i < blist.size(); i++) {

							blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

							this.dependenciesBM.add(new Object[] { DependencyType.ACCESS,
									this.getTargetClassName(node.getQualifier().resolveTypeBinding()), methodName,
									blockIndexTemp, getClassName(), lineBlock,
									fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
									node.getLength() });
						}
					}

					if (!isInterface)
						this.dependenciesCP.add(new Object[] { DependencyType.ACCESS,
								this.getTargetClassName(node.getQualifier().resolveTypeBinding()) });
					break;
				}

			}

		}
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		if (!isVisitingParent) {
			return super.visit(node);
		}
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		if (!isVisitingParent) {
			return super.visit(node);
		}
		return true;
	}

	public boolean visit(org.eclipse.jdt.core.dom.NormalAnnotation node) {
		if (!isVisitingParent) {
			if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
				// FieldDeclaration field = (FieldDeclaration) node.getParent();
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
							node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });
					}
				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName() });
			} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
				// MethodDeclaration method = (MethodDeclaration)
				// node.getParent();
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
							node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });
					}
				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName() });
			} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, getClassName(),
							lineMethod, fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
							node.getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndex,
							getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.USEANNOTATION,
								node.getTypeName().resolveTypeBinding().getQualifiedName(), methodName, blockIndexTemp,
								getClassName(), lineBlock, fullClass.getLineNumber(node.getStartPosition()),
								node.getStartPosition(), node.getLength() });
					}
				}

				if (!isInterface)
					this.dependenciesCP.add(new Object[] { DependencyType.USEANNOTATION,
							node.getTypeName().resolveTypeBinding().getQualifiedName() });
			}
		}
		return true;
	};

	@Override
	public boolean visit(ParameterizedType node) {
		if (!isVisitingParent) {
			ASTNode relevantParent = this.getRelevantParent(node);
			if (node.getNodeType() == ASTNode.PARAMETERIZED_TYPE) {
				ParameterizedType pt = (ParameterizedType) node;
				if (pt.typeArguments() != null) {
					for (Object o : pt.typeArguments()) {
						Type t = (Type) o;
						if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
							// MethodDeclaration md = (MethodDeclaration)
							// relevantParent;
							// TODO: esse if else nao serve pra nada
							if (MC && !isInterface && !isGetterOrSetter)
								this.dependenciesMC.add(new Object[] { DependencyType.DECLARE,
										this.getTargetClassName(t.resolveBinding()), methodName, getClassName(),
										lineMethod, fullClass.getLineNumber(t.getStartPosition()), t.getStartPosition(),
										t.getLength() });

							if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

								this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
										this.getTargetClassName(t.resolveBinding()), methodName, blockIndex,
										getClassName(), lineBlock, fullClass.getLineNumber(t.getStartPosition()),
										t.getStartPosition(), t.getLength() });

								String blockIndexTemp = blockIndex;

								for (int i = 1; i < blist.size(); i++) {

									blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

									this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
											this.getTargetClassName(t.resolveBinding()), methodName, blockIndexTemp,
											getClassName(), lineBlock, fullClass.getLineNumber(t.getStartPosition()),
											t.getStartPosition(), t.getLength() });
								}
							}
							if (!isInterface)
								this.dependenciesCP.add(new Object[] { DependencyType.DECLARE,
										this.getTargetClassName(t.resolveBinding()) });
						} else {
							if (MC && !isInterface && !isGetterOrSetter)
								this.dependenciesMC.add(new Object[] { DependencyType.DECLARE,
										this.getTargetClassName(t.resolveBinding()), methodName, getClassName(),
										lineMethod, fullClass.getLineNumber(t.getStartPosition()), t.getStartPosition(),
										t.getLength() });

							if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

								this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
										this.getTargetClassName(t.resolveBinding()), methodName, blockIndex,
										getClassName(), lineBlock, fullClass.getLineNumber(t.getStartPosition()),
										t.getStartPosition(), t.getLength() });

								String blockIndexTemp = blockIndex;

								for (int i = 1; i < blist.size(); i++) {

									blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

									this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
											this.getTargetClassName(t.resolveBinding()), methodName, blockIndexTemp,
											getClassName(), lineBlock, fullClass.getLineNumber(t.getStartPosition()),
											t.getStartPosition(), t.getLength() });
								}
							}
							if (!isInterface)
								this.dependenciesCP.add(new Object[] { DependencyType.DECLARE,
										this.getTargetClassName(t.resolveBinding()) });
						}
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
		if (type != null) {
			if (!type.isAnonymous() && type.getQualifiedName() != null && !type.getQualifiedName().isEmpty()) {
				result = type.getQualifiedName();
			} else if (type.isLocal() && type.getName() != null && !type.getName().isEmpty()) {
				result = type.getName();
			} else if (type.getSuperclass() != null
					&& !type.getSuperclass().getQualifiedName().equals("java.lang.Object")
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
		}
		return result;
	}

	@Override
	public boolean visit(CastExpression node) {
		if (!isVisitingParent) {
			Type t = node.getType();
			if (MC && !isInterface && !isGetterOrSetter)
				this.dependenciesMC.add(new Object[] { DependencyType.DECLARE,
						this.getTargetClassName(t.resolveBinding()), methodName, getClassName(), lineMethod,
						fullClass.getLineNumber(t.getStartPosition()), t.getStartPosition(), t.getLength() });

			if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

				this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
						this.getTargetClassName(t.resolveBinding()), methodName, blockIndex, getClassName(), lineBlock,
						fullClass.getLineNumber(t.getStartPosition()), t.getStartPosition(), t.getLength() });

				String blockIndexTemp = blockIndex;

				for (int i = 1; i < blist.size(); i++) {

					blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

					this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
							this.getTargetClassName(t.resolveBinding()), methodName, blockIndexTemp, getClassName(),
							lineBlock, fullClass.getLineNumber(t.getStartPosition()), t.getStartPosition(),
							t.getLength() });
				}
			}
			if (!isInterface)
				this.dependenciesCP
						.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(t.resolveBinding()) });

			return super.visit(node);
		}
		return true;
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		if (!isVisitingParent) {
			ITypeBinding typeBinding = node.getRightOperand().resolveBinding();
			ASTNode relevantParent = getRelevantParent(node);

			switch (relevantParent.getNodeType()) {
			case ASTNode.METHOD_DECLARATION:
				MethodDeclaration md = (MethodDeclaration) relevantParent;
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(typeBinding),
							methodName, getClassName(), lineMethod,
							fullClass.getLineNumber(node.getRightOperand().getStartPosition()),
							node.getRightOperand().getStartPosition(), node.getRightOperand().getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(typeBinding),
							methodName, blockIndex, getClassName(), lineBlock,
							fullClass.getLineNumber(node.getRightOperand().getStartPosition()),
							node.getRightOperand().getStartPosition(), node.getRightOperand().getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
								this.getTargetClassName(typeBinding), methodName, blockIndexTemp, getClassName(),
								lineBlock, fullClass.getLineNumber(node.getRightOperand().getStartPosition()),
								node.getRightOperand().getStartPosition(), node.getRightOperand().getLength() });
					}
				}
				if (!isInterface)
					this.dependenciesCP
							.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(typeBinding) });

				break;
			case ASTNode.INITIALIZER:
				if (MC && !isInterface && !isGetterOrSetter)
					this.dependenciesMC.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(typeBinding),
							methodName, getClassName(), lineMethod,
							fullClass.getLineNumber(node.getRightOperand().getStartPosition()),
							node.getRightOperand().getStartPosition(), node.getRightOperand().getLength() });

				if (MC && bloco > 0 && !isInterface && !isGetterOrSetter) {

					this.dependenciesBM.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(typeBinding),
							methodName, blockIndex, getClassName(), lineBlock,
							fullClass.getLineNumber(node.getRightOperand().getStartPosition()),
							node.getRightOperand().getStartPosition(), node.getRightOperand().getLength() });

					String blockIndexTemp = blockIndex;

					for (int i = 1; i < blist.size(); i++) {

						blockIndexTemp = blockIndexTemp.substring(0, blockIndexTemp.lastIndexOf("-"));

						this.dependenciesBM.add(new Object[] { DependencyType.DECLARE,
								this.getTargetClassName(typeBinding), methodName, blockIndexTemp, getClassName(),
								lineBlock, fullClass.getLineNumber(node.getRightOperand().getStartPosition()),
								node.getRightOperand().getStartPosition(), node.getRightOperand().getLength() });
					}
				}
				if (!isInterface)
					this.dependenciesCP
							.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(typeBinding) });
				break;
			}
			return super.visit(node);
		}
		return true;
	}

}