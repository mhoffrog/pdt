/**
 * 
 */
package org.eclipse.php.internal.ui.search;

import org.eclipse.php.internal.core.ast.nodes.*;
import org.eclipse.php.internal.core.ast.nodes.BodyDeclaration.Modifier;

/**
 * Finds all implement occurrences of an extended class or an implemented interface.
 * 
 * @author Shalom
 *
 */
public class ImplementOccurrencesFinder extends AbstractOccurrencesFinder {

	public static final String ID = "ImplementOccurrencesFinder"; //$NON-NLS-1$
	private Identifier fIdentifier;
	private TypeDeclaration fTypeDeclaration;
	private ITypeBinding fBinding;

	/* (non-Javadoc)
	 * @see org.eclipse.php.internal.ui.search.IOccurrencesFinder#initialize(org.eclipse.php.internal.core.ast.nodes.Program, org.eclipse.php.internal.core.ast.nodes.ASTNode)
	 */
	public String initialize(Program root, ASTNode node) {
		fASTRoot = root;
		if (isIdendifier(node)) {
			fIdentifier = (Identifier) node;
			StructuralPropertyDescriptor locationInParent = fIdentifier.getLocationInParent();
			if (locationInParent != ClassDeclaration.SUPER_CLASS_PROPERTY && locationInParent != ClassDeclaration.INTERFACES_PROPERTY && locationInParent != InterfaceDeclaration.INTERFACES_PROPERTY) {
				return "ImplementOccurrencesFinder_invalidTarget";
			}
			IBinding resolvedBinding = fIdentifier.resolveBinding();
			if (resolvedBinding == null || !(resolvedBinding instanceof ITypeBinding)) {
				return "ImplementOccurrencesFinder_invalidTarget";
			} else {
				fBinding = (ITypeBinding) resolvedBinding;
			}
			fTypeDeclaration = (TypeDeclaration) fIdentifier.getParent();
			return null;
		}
		fDescription = "OccurrencesFinder_occurrence_description";
		return fDescription;
	}

	/*
	 * Check if the node is an identifier node.
	 */
	private final boolean isIdendifier(ASTNode node) {
		return node != null && node.getType() == ASTNode.IDENTIFIER;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.php.internal.ui.search.AbstractOccurrencesFinder#findOccurrences()
	 */
	@Override
	protected void findOccurrences() {
		fDescription = Messages.format("Implementors of methods in ''{0}''", fIdentifier.getName());
		fTypeDeclaration.accept(this);
		fResult.add(new OccurrenceLocation(fIdentifier.getStart(), fIdentifier.getLength(), getOccurrenceType(fIdentifier), fDescription));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.MethodDeclaration)
	 */
	public boolean visit(MethodDeclaration methodDeclaration) {
		IMethodBinding methodBinding = methodDeclaration.resolveMethodBinding();
		if (methodBinding != null && !Modifier.isStatic(methodBinding.getModifiers())) {
			IMethodBinding method = Bindings.findOverriddenMethodInHierarchy(fBinding, methodBinding);
			if (method != null) {
				Identifier name = methodDeclaration.getFunction().getFunctionName();
				fResult.add(new OccurrenceLocation(name.getStart(), name.getLength(), 0, fDescription));
			}
		}
		return super.visit(methodDeclaration);
	}

	/**
	 * Always returns IOccurrencesFinder.K_IMPLEMENTS_OCCURRENCE.
	 * 
	 * @see AbstractOccurrencesFinder#getOccurrenceType(ASTNode)
	 * @see IOccurrencesFinder#K_IMPLEMENTS_OCCURRENCE
	 */
	protected int getOccurrenceType(ASTNode node) {
		return IOccurrencesFinder.K_IMPLEMENTS_OCCURRENCE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.php.internal.ui.search.IOccurrencesFinder#getElementName()
	 */
	public String getElementName() {
		return fIdentifier.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.php.internal.ui.search.IOccurrencesFinder#getID()
	 */
	public String getID() {
		return ID;
	}
}