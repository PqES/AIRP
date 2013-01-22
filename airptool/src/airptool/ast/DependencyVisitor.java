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
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
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

import airptool.enums.DependencyType;

public class DependencyVisitor extends ASTVisitor {
	private List<Object[]> dependencies;

	private ICompilationUnit unit;
	private CompilationUnit fullClass;
	private String className;

	public DependencyVisitor(ICompilationUnit unit) {
		this.dependencies = new ArrayList<Object[]>();
		this.unit = unit;

		this.className = unit.getParent().getElementName() + "." + unit.getElementName().substring(0, unit.getElementName().length() - 5);
		ASTParser parser = ASTParser.newParser(AST.JLS4); // It was JSL3, but it
															// is now deprecated
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);

		this.fullClass = (CompilationUnit) parser.createAST(null); // parse
		this.fullClass.accept(this);
	}

	public final List<Object[]> getDependencies() {
		return this.dependencies;
	}

	public final String getClassName() {
		return this.className;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (!node.isLocalTypeDeclaration() && !node.isMemberTypeDeclaration()) { // Para
																					// evitar
																					// fazer
																					// v‡rias
																					// vezes
			try {
				IType type = (IType) unit.getTypes()[0];
				ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(null);

				IType[] typeSuperclasses = typeHierarchy.getAllSuperclasses(type);

				for (IType t : typeSuperclasses) {
					if (node.getSuperclassType() != null
							&& t.getFullyQualifiedName().equals(node.getSuperclassType().resolveBinding().getQualifiedName())) {
						this.dependencies.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName() });
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
									this.dependencies.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName() });
								} else {
									this.dependencies.add(new Object[] { DependencyType.EXTEND, t.getFullyQualifiedName() });
								}
								continue externo;
							}
							break;
						case ASTNode.PARAMETERIZED_TYPE:
							ParameterizedType pt = (ParameterizedType) it;
							if (t.getFullyQualifiedName().equals(pt.getType().resolveBinding().getBinaryName())) {
								if (!type.isInterface()) {
									this.dependencies.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName() });
								} else {
									this.dependencies.add(new Object[] { DependencyType.IMPLEMENT, t.getFullyQualifiedName() });
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
			this.dependencies
					.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			// MethodDeclaration method = (MethodDeclaration) node.getParent();
			this.dependencies
					.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			this.dependencies
					.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		} else if (node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			// VariableDeclarationStatement st = (VariableDeclarationStatement)
			// node.getParent();
			// VariableDeclarationFragment vdf = ((VariableDeclarationFragment)
			// st.fragments().get(0));
			ASTNode relevantParent = this.getRelevantParent(node);
			if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
				// MethodDeclaration md = (MethodDeclaration) relevantParent;
				this.dependencies.add(new Object[] { DependencyType.USEANNOTATION,
						node.getTypeName().resolveTypeBinding().getQualifiedName() });
			}
		} else if (node.getParent().getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
			// SingleVariableDeclaration sv = (SingleVariableDeclaration)
			// node.getParent();
			ASTNode relevantParent = this.getRelevantParent(node);
			if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
				// MethodDeclaration md = (MethodDeclaration) relevantParent;
				this.dependencies.add(new Object[] { DependencyType.USEANNOTATION,
						node.getTypeName().resolveTypeBinding().getQualifiedName() });
			}

		}
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
			// FieldDeclaration field = (FieldDeclaration) node.getParent();
			this.dependencies
					.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			// MethodDeclaration method = (MethodDeclaration) node.getParent();
			this.dependencies
					.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			this.dependencies
					.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		ASTNode relevantParent = getRelevantParent(node);

		switch (relevantParent.getNodeType()) {
		case ASTNode.FIELD_DECLARATION:
			// FieldDeclaration fd = (FieldDeclaration) relevantParent;
			this.dependencies.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()) });
			break;
		case ASTNode.METHOD_DECLARATION:
			// MethodDeclaration md = (MethodDeclaration) relevantParent;
			this.dependencies.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()) });
			break;
		case ASTNode.INITIALIZER:
			this.dependencies.add(new Object[] { DependencyType.CREATE, this.getTargetClassName(node.getType().resolveBinding()) });
			break;
		}

		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		this.dependencies.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(node.getType().resolveBinding()) });
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		for (Object o : node.parameters()) {
			if (o instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
				this.dependencies.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(svd.getType().resolveBinding()) });
				if (svd.getType().getNodeType() == Type.PARAMETERIZED_TYPE) {
					// TODO: Adjust the way that we handle parameter types
					for (Object t : ((ParameterizedType) svd.getType()).typeArguments()) {
						if (t instanceof SimpleType) {
							SimpleType st = (SimpleType) t;
							this.dependencies.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(st.resolveBinding()) });
						} else if (t instanceof ParameterizedType) {
							ParameterizedType pt = (ParameterizedType) t;
							this.dependencies.add(new Object[] { DependencyType.DECLARE,
									this.getTargetClassName(pt.getType().resolveBinding()) });
						}
					}
				}

			}
		}
		for (Object o : node.thrownExceptions()) {
			Name name = (Name) o;
			this.dependencies.add(new Object[] { DependencyType.THROW, this.getTargetClassName(name.resolveTypeBinding()) });
		}

		if (node.getReturnType2() != null
				&& !(node.getReturnType2().isPrimitiveType() && ((PrimitiveType) node.getReturnType2()).getPrimitiveTypeCode() == PrimitiveType.VOID)) {
			if (!node.getReturnType2().resolveBinding().isTypeVariable()) {
				this.dependencies.add(new Object[] { DependencyType.DECLARE,
						this.getTargetClassName(node.getReturnType2().resolveBinding()) });
			} else {
				if (node.getReturnType2().resolveBinding().getTypeBounds().length >= 1) {
					this.dependencies.add(new Object[] { DependencyType.DECLARE,
							this.getTargetClassName(node.getReturnType2().resolveBinding().getTypeBounds()[0]) });
				}
			}

		}
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ASTNode relevantParent = getRelevantParent(node);

		switch (relevantParent.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			// MethodDeclaration md = (MethodDeclaration) relevantParent;
			this.dependencies.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(node.getType().resolveBinding()) });

			break;
		case ASTNode.INITIALIZER:
			this.dependencies.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(node.getType().resolveBinding()) });
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
				this.dependencies.add(new Object[] { DependencyType.ACCESS,
						this.getTargetClassName(node.getExpression().resolveTypeBinding()) });
			} else if (node.resolveMethodBinding() != null) {
				this.dependencies.add(new Object[] { DependencyType.ACCESS,
						this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()) });
			}
			break;
		case ASTNode.INITIALIZER:
			if (node.getExpression() != null) {
				this.dependencies.add(new Object[] { DependencyType.ACCESS,
						this.getTargetClassName(node.getExpression().resolveTypeBinding()) });
			} else if (node.resolveMethodBinding() != null) {
				this.dependencies.add(new Object[] { DependencyType.ACCESS,
						this.getTargetClassName(node.resolveMethodBinding().getDeclaringClass()) });
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
			this.dependencies
					.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()) });
			break;
		case ASTNode.INITIALIZER:
			this.dependencies
					.add(new Object[] { DependencyType.ACCESS, this.getTargetClassName(node.getExpression().resolveTypeBinding()) });
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
				this.dependencies.add(new Object[] { DependencyType.ACCESS,
						this.getTargetClassName(node.getQualifier().resolveTypeBinding()) });
				break;
			case ASTNode.INITIALIZER:
				this.dependencies.add(new Object[] { DependencyType.ACCESS,
						this.getTargetClassName(node.getQualifier().resolveTypeBinding()) });
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
			this.dependencies
					.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			// MethodDeclaration method = (MethodDeclaration) node.getParent();
			this.dependencies
					.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
		} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			this.dependencies
					.add(new Object[] { DependencyType.USEANNOTATION, node.getTypeName().resolveTypeBinding().getQualifiedName() });
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
						this.dependencies.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(t.resolveBinding()) });
					} else {
						this.dependencies.add(new Object[] { DependencyType.DECLARE, this.getTargetClassName(t.resolveBinding()) });
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
