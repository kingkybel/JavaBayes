/* BIFv01.java */
/* Generated By:JavaCC: Do not edit this line. BIFv01.java */
package Parsers.BIFv01;

import InterchangeFormat.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Definition of the Interchange Format class and its variables. The IFBayesNet
 * ifbn contains the parsed Bayesian network.
 * This parser uses the data structures in the JavaBayes core engine (package 
 * BayesianNetworks); other implementations may use different data structures.
 */
public class BIFv01 extends InterchangeFormat implements BIFv01Constants {
    IFBayesNet ifbn;

    public IFBayesNet getBayesNetFromInterchangeFmt()
    {
        return(ifbn);
    }

    /* Method responsible for globbing undefined text in an input file */
    void globUndefinedText() throws ParseException
    {
        Token t;
        while (true)
        {
            t = getToken(1);
            if ((t.kind == 0) ||
                (t.kind == NETWORK) ||
                (t.kind == VARIABLE) ||
                (t.kind == PROBABILITY))
            {
                    break;
            }
            else
            {
                getNextToken();
            }
          }
  }

// ==========================================================
// THE INTERCHANGE FORMAT GRAMMAR STARTS HERE
// ==========================================================

/**
 * Basic parsing function. First looks for a Network Declaration,
 * then looks for an arbitrary number of VariableDeclaration or
 * ProbabilityDeclaration non-terminals. The objects are
 * in the vectors ifbn.pvs and ifbn.upfs.
 */
  final public void CompilationUnit() throws ParseException {IFProbabilityVariable pv;
    IFProbabilityFunction upf;
globUndefinedText();
    NetworkDeclaration();
globUndefinedText();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case VARIABLE:
      case PROBABILITY:{
        ;
        break;
        }
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case VARIABLE:{
        pv = VariableDeclaration();
ifbn.add(pv); globUndefinedText();
        break;
        }
      case PROBABILITY:{
        upf = ProbabilityDeclaration();
ifbn.add(upf); globUndefinedText();
        break;
        }
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    jj_consume_token(0);
  }

/**
 * Detect and initialize the network.
 */
  final public void NetworkDeclaration() throws ParseException {Token t;
    ArrayList<String> properties;
    jj_consume_token(NETWORK);
    t = jj_consume_token(WORD);
    properties = NetworkContent();
ifbn = new IFBayesNet(t.image, properties);
  }

/**
 * Fill the network list of properties.
 */
  final public ArrayList<String> NetworkContent() throws ParseException {ArrayList<String> properties = new ArrayList<String>();
    String s;
    jj_consume_token(22);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case PROPERTYSTRING:{
        ;
        break;
        }
      default:
        jj_la1[2] = jj_gen;
        break label_2;
      }
      s = Property();
properties.add(s);
    }
    jj_consume_token(23);
{if ("" != null) return(properties);}
    throw new Error("Missing return statement in function");
  }

/**
 * Detect a variable declaration.
 */
  final public IFProbabilityVariable VariableDeclaration() throws ParseException {String s;
    IFProbabilityVariable pv;
    jj_consume_token(VARIABLE);
    s = ProbabilityVariableName();
    pv = VariableContent(s);
{if ("" != null) return(pv);}
    throw new Error("Missing return statement in function");
  }

/**
 * Fill a variable list of properties.
 */
  final public IFProbabilityVariable VariableContent(String name) throws ParseException {String s;
    String values[] = null;
    ArrayList<String> properties = new ArrayList<String>();
    IFProbabilityVariable pv = new IFProbabilityVariable();
    jj_consume_token(22);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case VARIABLETYPE:
      case PROPERTYSTRING:{
        ;
        break;
        }
      default:
        jj_la1[3] = jj_gen;
        break label_3;
      }
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case PROPERTYSTRING:{
        s = Property();
properties.add(s);
        break;
        }
      case VARIABLETYPE:{
        values = VariableDiscrete();
        break;
        }
      default:
        jj_la1[4] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    jj_consume_token(23);
pv.setName(name);
          pv.setProperties(properties);
          pv.setValues(values);
          {if ("" != null) return(pv);}
    throw new Error("Missing return statement in function");
  }

/**
 * Fill a variable type discrete.
 */
  final public String[] VariableDiscrete() throws ParseException {String values[] = null;
    jj_consume_token(VARIABLETYPE);
    jj_consume_token(DISCRETE);
    jj_consume_token(24);
    jj_consume_token(NUMBER);
    jj_consume_token(25);
    jj_consume_token(22);
    values = VariableValuesList();
    jj_consume_token(23);
    jj_consume_token(26);
{if ("" != null) return(values);}
    throw new Error("Missing return statement in function");
  }

/**
 * Get the values of a discrete variable.
 */
  final public String[] VariableValuesList() throws ParseException {int i;
    String value;
    String values[] = null;
    ArrayList<String> v = new ArrayList<String>();
    Iterator e;
    value = ProbabilityVariableValue();
v.add(value);
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case WORD:
      case NUMBER:{
        ;
        break;
        }
      default:
        jj_la1[5] = jj_gen;
        break label_4;
      }
      value = ProbabilityVariableValue();
v.add(value);
    }
values = new String[v.size()];
            for (e=v.iterator(), i=0; e.hasNext(); i++)
            {
                values[i] = (String)(e.next());
            }
            {if ("" != null) return(values);}
    throw new Error("Missing return statement in function");
  }

/**
 * Pick a single word as a probability variable value.
 */
  final public String ProbabilityVariableValue() throws ParseException {Token t;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case WORD:{
      t = jj_consume_token(WORD);
      break;
      }
    case NUMBER:{
      t = jj_consume_token(NUMBER);
      break;
      }
    default:
      jj_la1[6] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
{if ("" != null) return(t.image);}
    throw new Error("Missing return statement in function");
  }

/**
 * Detect a probability declaration.
 */
  final public IFProbabilityFunction ProbabilityDeclaration() throws ParseException {String vs[];
    IFProbabilityFunction upf = new IFProbabilityFunction();
    jj_consume_token(PROBABILITY);
    ProbabilityVariablesList(upf);
    ProbabilityContent(upf);
{if ("" != null) return(upf);}
    throw new Error("Missing return statement in function");
  }

/**
 * Parse the list of Probability variables.
 */
  final public void ProbabilityVariablesList(IFProbabilityFunction upf) throws ParseException {int i;
    Iterator e;
    String variable_name;
    int cond = -1;
    String vs[];
    ArrayList<String> v_list = new ArrayList<String>();
    jj_consume_token(27);
    variable_name = ProbabilityVariableName();
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 29:{
      cond = ConditionalMark(v_list);
      break;
      }
    default:
      jj_la1[7] = jj_gen;
      ;
    }
v_list.add(variable_name);
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case WORD:{
        ;
        break;
        }
      default:
        jj_la1[8] = jj_gen;
        break label_5;
      }
      variable_name = ProbabilityVariableName();
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 29:{
        cond = ConditionalMark(v_list);
        break;
        }
      default:
        jj_la1[9] = jj_gen;
        ;
      }
v_list.add(variable_name);
    }
    jj_consume_token(28);
vs = new String[v_list.size()];
            for (e=v_list.iterator(), i=0; e.hasNext(); i++)
            {
                vs[i] = (String)(e.next());
            }
            upf.setVariables(vs);
            upf.setConditionalIndex(cond);
  }

/**
 *  Find the conditional mark.
 */
  final public int ConditionalMark(ArrayList v) throws ParseException {
    jj_consume_token(29);
{if ("" != null) return(v.size());}
    throw new Error("Missing return statement in function");
  }

/**
 * Pick a single word as a probability variable name.
 */
  final public String ProbabilityVariableName() throws ParseException {Token t;
    t = jj_consume_token(WORD);
{if ("" != null) return(t.image);}
    throw new Error("Missing return statement in function");
  }

/**
 * Fill a Probability list of properties.
 */
  final public void ProbabilityContent(IFProbabilityFunction upf) throws ParseException {String s = null;
    ArrayList<String> properties = new ArrayList<String>();
    IFProbabilityEntry e = null;
    ArrayList entries = new ArrayList();
    ArrayList defs = new ArrayList();
    ArrayList tabs = new ArrayList();
    double def[] = null;
    double tab[] = null;
    jj_consume_token(22);
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case DEFAULTVALUE:
      case TABLEVALUES:
      case PROPERTYSTRING:
      case 27:{
        ;
        break;
        }
      default:
        jj_la1[10] = jj_gen;
        break label_6;
      }
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case PROPERTYSTRING:{
        s = Property();
properties.add(s);
        break;
        }
      case DEFAULTVALUE:{
        def = ProbabilityDefaultEntry();
defs.add(def);
        break;
        }
      case 27:{
        e = ProbabilityEntry();
entries.add(e);
        break;
        }
      case TABLEVALUES:{
        tab = ProbabilityTable();
tabs.add(tab);
        break;
        }
      default:
        jj_la1[11] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    jj_consume_token(23);
upf.setProperties(properties);
          upf.setDefaults(defs);
          upf.setEntries(entries);
          upf.setTables(tabs);
  }

/**
 * Pick a probability entry.
 */
  final public IFProbabilityEntry ProbabilityEntry() throws ParseException {String s[];
    double d[];
    s = ProbabilityValuesList();
    d = FloatingPointList();
    jj_consume_token(26);
{if ("" != null) return( new IFProbabilityEntry(s, d) );}
    throw new Error("Missing return statement in function");
  }

/**
 * Parse the list of Probability values in an entry.
 */
  final public String[] ProbabilityValuesList() throws ParseException {int i;
    Iterator e;
    String variable_name;
    String vs[];
    ArrayList<String> v_list = new ArrayList<String>();
    jj_consume_token(27);
    variable_name = ProbabilityVariableValue();
v_list.add(variable_name);
    label_7:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case WORD:
      case NUMBER:{
        ;
        break;
        }
      default:
        jj_la1[12] = jj_gen;
        break label_7;
      }
      variable_name = ProbabilityVariableValue();
v_list.add(variable_name);
    }
    jj_consume_token(28);
vs = new String[v_list.size()];
            for (e=v_list.iterator(), i=0; e.hasNext(); i++)
            {
                vs[i] = (String)(e.next());
            }
            {if ("" != null) return(vs);}
    throw new Error("Missing return statement in function");
  }

  final public double[] ProbabilityDefaultEntry() throws ParseException {double d[];
    jj_consume_token(DEFAULTVALUE);
    d = FloatingPointList();
    jj_consume_token(26);
{if ("" != null) return(d);}
    throw new Error("Missing return statement in function");
  }

  final public double[] ProbabilityTable() throws ParseException {double d[];
    jj_consume_token(TABLEVALUES);
    d = FloatingPointList();
    jj_consume_token(26);
{if ("" != null) return(d);}
    throw new Error("Missing return statement in function");
  }

// ======================================================
//          Some general purpose non-terminals 
// ======================================================

/**
 * Pick a list of non-negative floating numbers.
 */
  final public double[] FloatingPointList() throws ParseException {int i;
    Double d;
    double ds[];
    ArrayList<Double> d_list = new ArrayList<Double>();
    Iterator e;
    d = FloatingPointNumber();
d_list.add(d);
    label_8:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case NUMBER:{
        ;
        break;
        }
      default:
        jj_la1[13] = jj_gen;
        break label_8;
      }
      d = FloatingPointNumber();
d_list.add(d);
    }
ds = new double[d_list.size()];
            for (e=d_list.iterator(), i=0; e.hasNext(); i++)
            {
                d = (Double)(e.next());
                ds[i] = d.doubleValue();
            }
            {if ("" != null) return(ds);}
    throw new Error("Missing return statement in function");
  }

/**
 * Pick a non-negative floating number; necessary to allow
 * ignored characters and comments to exist in the middle
 * of a FloatingPointList().
 */
  final public Double FloatingPointNumber() throws ParseException {Token t;
    t = jj_consume_token(NUMBER);
{if ("" != null) return( Double.valueOf(t.image) );}
    throw new Error("Missing return statement in function");
  }

/**
 * Property definition.
 */
  final public String Property() throws ParseException {int k;
    Token t;
    String s;
    t = jj_consume_token(PROPERTYSTRING);
s = t.image;
            k = s.indexOf(' ');
            {if ("" != null) return(s.substring(k, s.length() - 1));}
    throw new Error("Missing return statement in function");
  }

  /** Generated Token Manager. */
  public BIFv01TokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[14];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x600,0x600,0x10000,0x11000,0x11000,0x120000,0x120000,0x20000000,0x20000,0x20000000,0x801c000,0x801c000,0x120000,0x100000,};
   }

  /** Constructor with InputStream. */
  public BIFv01(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public BIFv01(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new BIFv01TokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public BIFv01(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new BIFv01TokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public BIFv01(BIFv01TokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(BIFv01TokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk_f() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[30];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 14; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 30; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
