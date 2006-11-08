package com.atlassw.tools.eclipse.checkstyle.quickfixes.coding;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;
import org.junit.*;
import org.junit.Assert;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.Utility;

public class MissingSwitchDefaultTest {

	private String source = "public class A {\n" +
			"public void foo() {\n" +
			"switch(a) {\n" +
			"case 1: bar(); break;\n" +
			"case 2:\n" +
			"switch(b) {\n" +
			"case 3: bar(); break;\n" +
			"}; break;\n" +
			"}\n" +
			"}\n" +
			"}\n";


	private MissingSwitchDefaultQuickfix fix;

	@Before
	public void setUp()
	{
		fix = new MissingSwitchDefaultQuickfix();
	}
	
	@Test
	public void testMissingSwitch() throws Exception
	{
		String expected1 = "public class A {\n" +
		"public void foo() {\n" +
		"switch(a) {\n" +
		"case 1: bar(); break;\n" +
		"case 2:\n" +
		"switch(b) {\n" +
		"case 3: bar(); break;\n" +
		"}; break;\n" +
		"	default:\n" + 
		"		// TODO add default case statements\n" +
		"}\n" +
		"}\n" +
		"}\n";
		Utility.commonTestFix(source, expected1, fix, 2);
	}
	
	@Test
	public void testMissingSwitchInner() throws Exception
	{
		String expected2 = "public class A {\n" +
		"public void foo() {\n" +
		"switch(a) {\n" +
		"case 1: bar(); break;\n" +
		"case 2:\n" +
		"switch(b) {\n" +
		"case 3: bar(); break;\n" +
		"	default:\n" + 
		"		// TODO add default case statements\n" +
		"}; break;\n" +
		"}\n" +
		"}\n" +
		"}\n";

		Utility.commonTestFix(source, expected2, fix, 5);
	}

}
