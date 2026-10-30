/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The interpreted document model: port of `jsesh.model`.
///
/// ```
/// HieroglyphicText              the whole text
///   └─ TopItem
///        ├─ BasicItem: Cadrat, AlphabeticCharacter, MdcComment
///        │    Cadrat └─ HBox └─ HorizontalListElement
///        │                        ├─ InnerGroup: Hieroglyph, Ligature, AbsoluteGroup,
///        │                        │   Overwrite, Insert, Kerning, SubCadrat, Cartouche, Philology
///        │                        └─ ComplexLigature
///        ├─ Superscript, LineBreak, PageBreak, TabStop, HRule
///        └─ Zone └─ ZoneContent └─ TopItem (nested zones too)
/// ```
///
/// Elements are mutable and own their children. Every change is reported as a
/// [ModelOperation], which bubbles up to the [HieroglyphicText]'s observers.

import {
  ShadingCode,
  SymbolCodes,
  scriptForMdcCode,
  typeForCode,
  type CartouchePart,
  type CartoucheType,
  type InsertionDirection,
  type KerningAxis,
  type ScriptCode,
  type TextDirection,
  type TextOrientation,
  type WordEndingCode,
} from "./constants.js";

// ---- Change notification (port of jsesh.model.operations)

export type ModelOperation =
  | { kind: "insertion"; element: ModelElement; index: number; children: ModelElement[] }
  | { kind: "deletion"; element: ModelElement; start: number; end: number }
  | { kind: "replacement"; element: ModelElement; index: number }
  | { kind: "modification"; element: ModelElement }
  | { kind: "child"; element: ModelElement; operation: ModelOperation };

export type ModelObserver = (operation: ModelOperation) => void;

export interface ModelElementVisitor<R = void> {
  visitHieroglyphicText(e: HieroglyphicText): R;
  visitCadrat(e: Cadrat): R;
  visitHBox(e: HBox): R;
  visitHieroglyph(e: Hieroglyph): R;
  visitModifiersList(e: ModifiersList): R;
  visitModifier(e: Modifier): R;
  visitLigature(e: Ligature): R;
  visitAbsoluteGroup(e: AbsoluteGroup): R;
  visitOverwrite(e: Overwrite): R;
  visitInsert(e: Insert): R;
  visitKerning(e: Kerning): R;
  visitComplexLigature(e: ComplexLigature): R;
  visitSubCadrat(e: SubCadrat): R;
  visitBasicItemList(e: BasicItemList): R;
  visitCartouche(e: Cartouche): R;
  visitPhilology(e: Philology): R;
  visitAlphabeticCharacter(e: AlphabeticCharacter): R;
  visitComment(e: MdcComment): R;
  visitSuperscript(e: Superscript): R;
  visitLineBreak(e: LineBreak): R;
  visitPageBreak(e: PageBreak): R;
  visitTabStop(e: TabStop): R;
  visitHRule(e: HRule): R;
  visitZone(e: Zone): R;
  visitZoneContent(e: ZoneContent): R;
}

/// A visitor whose methods all call [ModelElementAdapter.visitDefault]
/// (port of `jsesh.model.ModelElementAdapter`).
export class ModelElementAdapter implements ModelElementVisitor<void> {
  visitDefault(_e: ModelElement): void {}
  visitHieroglyphicText(e: HieroglyphicText): void {
    this.visitDefault(e);
  }
  visitCadrat(e: Cadrat): void {
    this.visitDefault(e);
  }
  visitHBox(e: HBox): void {
    this.visitDefault(e);
  }
  visitHieroglyph(e: Hieroglyph): void {
    this.visitDefault(e);
  }
  visitModifiersList(e: ModifiersList): void {
    this.visitDefault(e);
  }
  visitModifier(e: Modifier): void {
    this.visitDefault(e);
  }
  visitLigature(e: Ligature): void {
    this.visitDefault(e);
  }
  visitAbsoluteGroup(e: AbsoluteGroup): void {
    this.visitDefault(e);
  }
  visitOverwrite(e: Overwrite): void {
    this.visitDefault(e);
  }
  visitInsert(e: Insert): void {
    this.visitDefault(e);
  }
  visitKerning(e: Kerning): void {
    this.visitDefault(e);
  }
  visitComplexLigature(e: ComplexLigature): void {
    this.visitDefault(e);
  }
  visitSubCadrat(e: SubCadrat): void {
    this.visitDefault(e);
  }
  visitBasicItemList(e: BasicItemList): void {
    this.visitDefault(e);
  }
  visitCartouche(e: Cartouche): void {
    this.visitDefault(e);
  }
  visitPhilology(e: Philology): void {
    this.visitDefault(e);
  }
  visitAlphabeticCharacter(e: AlphabeticCharacter): void {
    this.visitDefault(e);
  }
  visitComment(e: MdcComment): void {
    this.visitDefault(e);
  }
  visitSuperscript(e: Superscript): void {
    this.visitDefault(e);
  }
  visitLineBreak(e: LineBreak): void {
    this.visitDefault(e);
  }
  visitPageBreak(e: PageBreak): void {
    this.visitDefault(e);
  }
  visitTabStop(e: TabStop): void {
    this.visitDefault(e);
  }
  visitHRule(e: HRule): void {
    this.visitDefault(e);
  }
  visitZone(e: Zone): void {
    this.visitDefault(e);
  }
  visitZoneContent(e: ZoneContent): void {
    this.visitDefault(e);
  }
}

export abstract class ModelElement {
  private readonly _children: ModelElement[] = [];
  private _parent: ModelElement | null = null;
  private updatesEnabled = true;

  abstract accept<R>(v: ModelElementVisitor<R>): R;
  abstract deepCopy(): ModelElement;

  get parent(): ModelElement | null {
    return this._parent;
  }

  /// The children, read-only. Use the methods of the subclasses to change them.
  get children(): readonly ModelElement[] {
    return this._children;
  }

  getChildAt(i: number): ModelElement {
    const c = this._children[i];
    if (c === undefined) throw new RangeError(`no child ${i} in ${this.constructor.name}`);
    return c;
  }

  get numberOfChildren(): number {
    return this._children.length;
  }

  get nextSibling(): ModelElement | null {
    if (this._parent === null) return null;
    const siblings = this._parent._children;
    return siblings[siblings.indexOf(this) + 1] ?? null;
  }

  get previousSibling(): ModelElement | null {
    if (this._parent === null) return null;
    const siblings = this._parent._children;
    return siblings[siblings.indexOf(this) - 1] ?? null;
  }

  /// The index of this element in its parent, or -1.
  indexInParent(): number {
    return this._parent === null ? -1 : this._parent._children.indexOf(this);
  }

  protected addChild(child: ModelElement): void {
    this.addChildAt(this._children.length, child);
  }

  protected addChildAt(index: number, child: ModelElement): void {
    child.detach();
    child._parent = this;
    this._children.splice(index, 0, child);
    this.notify({ kind: "insertion", element: this, index, children: [child] });
  }

  protected removeChildAt(index: number): ModelElement | undefined {
    if (index < 0 || index >= this._children.length) return undefined;
    const [child] = this._children.splice(index, 1);
    child!._parent = null;
    this.notify({ kind: "deletion", element: this, start: index, end: index + 1 });
    return child;
  }

  protected removeChild(child: ModelElement): void {
    this.removeChildAt(this._children.indexOf(child));
  }

  protected removeChildren(start: number, end: number): ModelElement[] {
    const removed = this._children.splice(start, end - start);
    for (const c of removed) c._parent = null;
    this.notify({ kind: "deletion", element: this, start, end });
    return removed;
  }

  protected setChildAt(index: number, child: ModelElement): void {
    child.detach();
    const old = this._children[index];
    if (old !== undefined) old._parent = null;
    child._parent = this;
    this._children[index] = child;
    this.notify({ kind: "replacement", element: this, index });
  }

  protected clearChildren(): void {
    for (const c of this._children) c._parent = null;
    this._children.length = 0;
    this.notify({ kind: "modification", element: this });
  }

  /// Removes this element from its parent, without notification (used when
  /// an element is moved).
  private detach(): void {
    if (this._parent !== null) {
      const siblings = this._parent._children;
      const i = siblings.indexOf(this);
      if (i >= 0) siblings.splice(i, 1);
      this._parent = null;
    }
  }

  protected copyChildrenTo(copy: ModelElement): void {
    for (const c of this._children) copy.addChild(c.deepCopy());
  }

  protected notifyModification(): void {
    this.notify({ kind: "modification", element: this });
  }

  protected disableUpdates(): void {
    this.updatesEnabled = false;
  }

  protected enableUpdates(): void {
    this.updatesEnabled = true;
  }

  protected notify(operation: ModelOperation): void {
    if (!this.updatesEnabled) return;
    if (this._parent !== null) {
      this._parent.notify({ kind: "child", element: this._parent, operation });
    } else {
      this.notifyObservers(operation);
    }
  }

  /// Called for root elements; only [HieroglyphicText] has observers.
  protected notifyObservers(_operation: ModelOperation): void {}

  containsOnlyOneSign(): boolean {
    return this._children.length === 1 && this._children[0]!.containsOnlyOneSign();
  }

  getLoneSign(): Hieroglyph {
    if (!this.containsOnlyOneSign()) throw new Error("Wrong call to getLoneSign.");
    return this._children[0]!.getLoneSign();
  }

  isBreak(): boolean {
    return false;
  }
}

// ---- Top items

export class TopItemState {
  constructor(
    public isRed = false,
    public isShaded = false,
  ) {}

  duplicate(): TopItemState {
    return new TopItemState(this.isRed, this.isShaded);
  }

  equals(other: TopItemState): boolean {
    return this.isRed === other.isRed && this.isShaded === other.isShaded;
  }
}

export abstract class TopItem extends ModelElement {
  state = new TopItemState();

  abstract override deepCopy(): TopItem;

  get isRed(): boolean {
    return this.state.isRed;
  }

  get isShaded(): boolean {
    return this.state.isShaded;
  }

  protected copyStateTo(item: TopItem): void {
    item.state = this.state.duplicate();
  }
}

/// A top item which can also be found inside cartouches, sub-cadrats...
export abstract class BasicItem extends TopItem {}

export abstract class HorizontalListElement extends ModelElement {
  abstract override deepCopy(): HorizontalListElement;
}

export abstract class InnerGroup extends HorizontalListElement {
  abstract override deepCopy(): InnerGroup;
}

export class HieroglyphicText extends ModelElement {
  private readonly observers = new Set<ModelObserver>();

  constructor(items: Iterable<TopItem> = []) {
    super();
    for (const item of items) this.addTopItem(item);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitHieroglyphicText(this);
  }

  get topItems(): readonly TopItem[] {
    return this.children as readonly TopItem[];
  }

  getTopItemAt(i: number): TopItem {
    return this.getChildAt(i) as TopItem;
  }

  addTopItem(item: TopItem): void {
    this.addChild(item);
  }

  addTopItemAt(index: number, item: TopItem): void {
    this.addChildAt(index, item);
  }

  addAll(items: Iterable<TopItem>): void {
    for (const item of items) this.addChild(item);
  }

  insertItemsAt(index: number, items: readonly TopItem[]): void {
    items.forEach((item, i) => this.addChildAt(index + i, item));
  }

  removeItems(start: number, end: number): TopItem[] {
    return this.removeChildren(start, end) as TopItem[];
  }

  removeTopItem(i: number): void {
    this.removeChildAt(i);
  }

  deepCopy(): HieroglyphicText {
    const copy = new HieroglyphicText();
    this.copyChildrenTo(copy);
    return copy;
  }

  addObserver(observer: ModelObserver): void {
    this.observers.add(observer);
  }

  deleteObserver(observer: ModelObserver): void {
    this.observers.delete(observer);
  }

  protected override notifyObservers(operation: ModelOperation): void {
    for (const o of this.observers) o(operation);
  }

  /// The sign codes used in the text, in order of first appearance.
  getCodes(): string[] {
    const codes = new Set<string>();
    const walk = (e: ModelElement) => {
      if (e instanceof Hieroglyph && e.type === SymbolCodes.MDCCODE) codes.add(e.code);
      for (const c of e.children) walk(c);
    };
    walk(this);
    return [...codes];
  }
}

export class Cadrat extends BasicItem {
  shading: number = ShadingCode.NONE;

  constructor(hBoxes: Iterable<HBox> = []) {
    super();
    for (const b of hBoxes) this.addHBox(b);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitCadrat(this);
  }

  get hBoxes(): readonly HBox[] {
    return this.children as readonly HBox[];
  }

  getHBox(i: number): HBox {
    return this.getChildAt(i) as HBox;
  }

  get numberOfHBoxes(): number {
    return this.numberOfChildren;
  }

  addHBox(b: HBox): void {
    this.addChild(b);
  }

  addHBoxAt(index: number, b: HBox): void {
    this.addChildAt(index, b);
  }

  removeHBox(b: HBox): void {
    this.removeChild(b);
  }

  setShading(shading: number): void {
    this.shading = shading;
    this.notifyModification();
  }

  isShadedPart(part: number): boolean {
    return (this.shading & part) !== 0;
  }

  /// Does this cadrat contain a "wide" sign (`\l` modifier)?
  isWide(): boolean {
    return this.hBoxes.some((b) => b.isWide());
  }

  /// If this quadrat only contains kerning operators, the axis of the last one.
  ///
  /// Such a quadrat is used to kern the quadrats around it, as in `p-%hkern-q`.
  getKerningAxis(): KerningAxis | undefined {
    let axis: KerningAxis | undefined;
    for (const b of this.hBoxes) {
      axis = b.getKerningAxis();
      if (axis === undefined) return undefined;
    }
    return axis;
  }

  /// Does this quadrat contain a kerning operator, at any level of its lines?
  containsKerning(): boolean {
    return this.hBoxes.some((b) => b.containsKerning());
  }

  /// Is this cadrat embedded in a sub-cadrat?
  isEmbedded(): boolean {
    for (let e = this.parent; e !== null; e = e.parent) {
      if (e instanceof SubCadrat) return true;
    }
    return false;
  }

  deepCopy(): Cadrat {
    const copy = new Cadrat();
    copy.shading = this.shading;
    this.copyChildrenTo(copy);
    this.copyStateTo(copy);
    return copy;
  }
}

export class HBox extends ModelElement {
  constructor(elements: Iterable<HorizontalListElement> = []) {
    super();
    for (const e of elements) this.addHorizontalListElement(e);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitHBox(this);
  }

  get elements(): readonly HorizontalListElement[] {
    return this.children as readonly HorizontalListElement[];
  }

  getElementAt(i: number): HorizontalListElement {
    return this.getChildAt(i) as HorizontalListElement;
  }

  addHorizontalListElement(e: HorizontalListElement): void {
    this.addChild(e);
  }

  addHorizontalListElementAt(index: number, e: HorizontalListElement): void {
    this.addChildAt(index, e);
  }

  removeHorizontalListElement(e: HorizontalListElement): void {
    this.removeChild(e);
  }

  /// A hbox is wide if it holds only one sign, with the `\l` modifier.
  isWide(): boolean {
    const only = this.numberOfChildren === 1 ? this.getChildAt(0) : null;
    return only instanceof Hieroglyph && only.isWide();
  }

  /// If this box only contains kerning operators, the axis of the last one.
  ///
  /// Such a box is a line of a quadrat used to kern the lines around it, as in `p:%vkern:q`.
  getKerningAxis(): KerningAxis | undefined {
    let axis: KerningAxis | undefined;
    for (const e of this.elements) {
      if (!(e instanceof Kerning)) return undefined;
      axis = e.axis;
    }
    return axis;
  }

  /// Does this box contain a kerning operator?
  containsKerning(): boolean {
    return this.elements.some((e) => e instanceof Kerning);
  }

  deepCopy(): HBox {
    const copy = new HBox();
    this.copyChildrenTo(copy);
    return copy;
  }
}

// ---- Signs

export class Modifier extends ModelElement {
  constructor(
    public name: string,
    public value: number | null,
  ) {
    super();
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitModifier(this);
  }

  deepCopy(): Modifier {
    return new Modifier(this.name, this.value);
  }

  override toString(): string {
    return "\\" + this.name + (this.value ?? "");
  }
}

/// The modifiers of a sign. Reversal, scale and rotation are kept as fields;
/// other modifiers (`\l`, `\i`, `\red`, `\shading1234`...) as [Modifier]
/// children.
export class ModifiersList extends ModelElement {
  angle = 0;
  reversed = false;
  /// Scale of the sign, in percent.
  scale = 100;

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitModifiersList(this);
  }

  get modifiers(): readonly Modifier[] {
    return this.children as readonly Modifier[];
  }

  private static arity(m: Modifier): number {
    return m.value === null ? 0 : 1;
  }

  private getModifierFrom(name: string, arity: number): Modifier | undefined {
    return this.modifiers.find((m) => m.name === name && (arity === -1 || ModifiersList.arity(m) === arity));
  }

  private addAux(m: Modifier): void {
    const m1 = this.getModifierFrom(m.name, ModifiersList.arity(m));
    if (m1 === undefined) {
      this.addChild(m);
    } else if (ModifiersList.arity(m) === 1) {
      m1.value = m.value;
      this.notifyModification();
    }
  }

  /// Adds a modifier, interpreting the ones which have a meaning of their own
  /// (`\` reverse, `\NN` scale, `\rN`, `\tN`, `\RN` rotations, `\sN` scale).
  includeModifier(m: Modifier): void {
    if (m.name === "") {
      if (m.value === null) this.reversed = true;
      else this.setScale(m.value);
    } else if (m.value !== null) {
      switch (m.name) {
        case "r":
          this.angle = (360 - m.value * 90) % 360;
          break;
        case "t":
          this.angle = (360 - m.value * 90) % 360;
          this.reversed = true;
          break;
        case "R":
          this.angle = m.value % 360;
          if (this.angle < 0) this.angle += 360;
          break;
        case "s": {
          let computedScale = 100;
          for (let i = 0; i < m.value; i++) computedScale = computedScale / 1.4142136;
          this.setScale(Math.trunc(computedScale));
          break;
        }
        default:
          this.addAux(m);
      }
    } else {
      this.addAux(m);
    }
  }

  removeModifier(m: Modifier): void {
    if (m.name === "") {
      if (m.value === null) this.reversed = false;
      else this.setScale(100);
    } else if (m.value !== null && ["r", "t", "R"].includes(m.name)) {
      this.angle = 0;
      if (m.name === "t") this.reversed = false;
    } else {
      const found = this.getModifierFrom(m.name, ModifiersList.arity(m));
      if (found) this.removeChild(found);
    }
  }

  /// Is the flag modifier `name` set? The empty name means "reversed".
  getBoolean(name: string): boolean {
    return name === "" ? this.reversed : this.getModifierFrom(name, 0) !== undefined;
  }

  setBoolean(name: string, value: boolean): void {
    if (value) this.includeModifier(new Modifier(name, null));
    else this.removeModifier(new Modifier(name, null));
  }

  hasInteger(name: string): boolean {
    return this.getModifierFrom(name, 1) !== undefined;
  }

  getIntegerWithDefault(name: string, defaultValue: number): number {
    return this.getModifierFrom(name, 1)?.value ?? defaultValue;
  }

  setInteger(name: string, value: number): void {
    this.includeModifier(new Modifier(name, value));
  }

  unsetInteger(name: string): void {
    const m = this.getModifierFrom(name, 1);
    if (m) this.removeChild(m);
  }

  setScale(scale: number): void {
    this.scale = scale;
    this.notifyModification();
  }

  setAngle(angle: number): void {
    // Java quirk: the original normalises `a`, not the stored angle, after the modulo.
    this.angle = angle % 360;
    this.notifyModification();
  }

  setReversed(reversed: boolean): void {
    this.reversed = reversed;
    this.notifyModification();
  }

  deepCopy(): ModifiersList {
    const copy = new ModifiersList();
    copy.angle = this.angle;
    copy.reversed = this.reversed;
    copy.scale = this.scale;
    this.copyChildrenTo(copy);
    return copy;
  }
}

export class Hieroglyph extends InnerGroup {
  code = "A1";
  type: number = SymbolCodes.MDCCODE;
  grammar = false;
  endingCode: WordEndingCode = "NONE";
  x = 0;
  y = 0;

  constructor(code: string, modifiers: ModifiersList = new ModifiersList()) {
    super();
    this.addChild(modifiers);
    this.setCode(code);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitHieroglyph(this);
  }

  get modifiers(): ModifiersList {
    return this.getChildAt(0) as ModifiersList;
  }

  setModifiers(modifiers: ModifiersList): void {
    this.setChildAt(0, modifiers);
    this.notifyModification();
  }

  /// Sets the code. `J...` codes become `Aa...`, `Ff2` becomes `V49A`,
  /// the backtick becomes `Ff1`, and the type is deduced from the code.
  setCode(code: string): void {
    if (code.startsWith("J")) code = "Aa" + code.substring(1);
    else if (code === "Ff2") code = "V49A";
    else if (code === "`") code = "Ff1";
    this.code = code;
    const typeCode = typeForCode(code);
    this.type = typeCode !== -1 ? typeCode : SymbolCodes.MDCCODE;
    this.notifyModification();
  }

  setType(type: number): void {
    this.type = type;
    this.notifyModification();
  }

  setGrammar(grammar: boolean): void {
    this.grammar = grammar;
    this.notifyModification();
  }

  setEndingCode(endingCode: WordEndingCode): void {
    this.endingCode = endingCode;
    this.notifyModification();
  }

  setExplicitPosition(x: number, y: number, scale: number): void {
    this.x = x;
    this.y = y;
    this.modifiers.setScale(scale);
  }

  get angle(): number {
    return this.modifiers.angle;
  }

  get relativeSize(): number {
    return this.modifiers.scale;
  }

  get isReversed(): boolean {
    return this.modifiers.reversed;
  }

  /// A wide sign (`\l` modifier) is stretched to the quadrat width.
  isWide(): boolean {
    return this.modifiers.getBoolean("l");
  }

  isShadingSign(): boolean {
    return (
      this.type === SymbolCodes.HORIZONTALSHADE ||
      this.type === SymbolCodes.VERTICALSHADE ||
      this.type === SymbolCodes.FULLSHADE ||
      this.type === SymbolCodes.QUATERSHADE
    );
  }

  /// The text of a small text sign (`"text"`).
  getSmallText(): string {
    if (this.type !== SymbolCodes.SMALLTEXT) throw new Error("Incorrect call");
    return this.code.substring(this.code.indexOf('"') + 1, this.code.length - 1);
  }

  /// Is this sign alone in its quadrat?
  isAloneInQuadrat(): boolean {
    let e = this.parent;
    while (e !== null && !(e instanceof Cadrat)) e = e.parent;
    return e !== null && e.containsOnlyOneSign();
  }

  override containsOnlyOneSign(): boolean {
    return true;
  }

  override getLoneSign(): Hieroglyph {
    return this;
  }

  deepCopy(): Hieroglyph {
    const copy = new Hieroglyph(this.code, this.modifiers.deepCopy());
    copy.endingCode = this.endingCode;
    copy.grammar = this.grammar;
    copy.type = this.type;
    copy.x = this.x;
    copy.y = this.y;
    return copy;
  }
}

/// `A&B&C`.
export class Ligature extends InnerGroup {
  constructor(hieroglyphs: Iterable<Hieroglyph> = []) {
    super();
    for (const h of hieroglyphs) this.addHieroglyph(h);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitLigature(this);
  }

  get hieroglyphs(): readonly Hieroglyph[] {
    return this.children as readonly Hieroglyph[];
  }

  addHieroglyph(h: Hieroglyph): void {
    this.addChild(h);
  }

  override containsOnlyOneSign(): boolean {
    return false;
  }

  deepCopy(): Ligature {
    const copy = new Ligature();
    this.copyChildrenTo(copy);
    return copy;
  }
}

/// `A**B`: signs with explicit positions.
export class AbsoluteGroup extends InnerGroup {
  constructor(hieroglyphs: Iterable<Hieroglyph> = []) {
    super();
    for (const h of hieroglyphs) this.addHieroglyph(h);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitAbsoluteGroup(this);
  }

  get hieroglyphs(): readonly Hieroglyph[] {
    return this.children as readonly Hieroglyph[];
  }

  /// Shading signs are kept first, so that they are drawn below the others.
  addHieroglyph(h: Hieroglyph): void {
    if (h.isShadingSign()) {
      let i = 0;
      while (i < this.numberOfChildren && this.hieroglyphs[i]!.isShadingSign()) i++;
      this.addChildAt(i, h);
    } else {
      this.addChild(h);
    }
  }

  override containsOnlyOneSign(): boolean {
    return this.numberOfChildren === 1;
  }

  /// Moves the signs so that the smallest x and y are 0.
  compact(): void {
    if (this.numberOfChildren === 0) return;
    const x = Math.min(...this.hieroglyphs.map((h) => h.x));
    const y = Math.min(...this.hieroglyphs.map((h) => h.y));
    for (const h of this.hieroglyphs) h.setExplicitPosition(h.x - x, h.y - y, h.relativeSize);
  }

  deepCopy(): AbsoluteGroup {
    const copy = new AbsoluteGroup();
    this.copyChildrenTo(copy);
    return copy;
  }
}

/// `A##B`.
export class Overwrite extends InnerGroup {
  constructor(first: Hieroglyph, second: Hieroglyph) {
    super();
    this.addChild(first);
    this.addChild(second);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitOverwrite(this);
  }

  get first(): Hieroglyph {
    return this.getChildAt(0) as Hieroglyph;
  }

  get second(): Hieroglyph {
    return this.getChildAt(1) as Hieroglyph;
  }

  override containsOnlyOneSign(): boolean {
    return false;
  }

  deepCopy(): Overwrite {
    return new Overwrite(this.first.deepCopy(), this.second.deepCopy());
  }
}

/// A group inserted into the free space of a sign, as in `%insert[dir=bs,dw=20](D36,(n:t))`
/// (`jsesh.model.Insert`).
///
/// An `Insert` has two children, both [SubCadrat]s, which may hold any group of signs
/// (including other insertions). They are kept in *reading order*, as in the text. One of them is
/// the *target* (or *main* element), the other one is the *inserted* element: `target` (1 or 2,
/// 1 by default) says which argument is the target, so that `%insert[dir=bs](A,B)` and
/// `%insert[dir=bs,target=2](B,A)` are the same insertion of `B` into `A`, `B` being read first
/// in the second one. The inserted
/// element is placed, and scaled, at layout time, so that it fits in the free space of the main
/// element. The main element's frame can be enlarged on the side of the insertion, to leave more
/// room: `dw` and `dh` are percentages of the inserted element's natural width and height.
export class Insert extends InnerGroup {
  private _direction: InsertionDirection;
  private _target: 1 | 2;
  private _dw = 0;
  private _dh = 0;

  /// @param first the first argument, in reading order.
  /// @param second the second argument, in reading order.
  /// @param target which argument is the target: 1 (the first one) or 2 (the second one).
  /// @param dw enlargement of the target's frame, horizontally, in percent of the inserted
  ///        element's width. Negative values are taken as 0.
  /// @param dh the same, vertically.
  constructor(direction: InsertionDirection, first: SubCadrat, second: SubCadrat, target: 1 | 2 = 1, dw = 0, dh = 0) {
    super();
    if (target !== 1 && target !== 2) throw new Error("insert target must be 1 or 2, not " + target);
    this._direction = direction;
    this._target = target;
    this._dw = Math.max(0, dw);
    this._dh = Math.max(0, dh);
    this.addChild(first);
    this.addChild(second);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitInsert(this);
  }

  /// The first argument, in reading order.
  get first(): SubCadrat {
    return this.getChildAt(0) as SubCadrat;
  }

  /// The second argument, in reading order.
  get second(): SubCadrat {
    return this.getChildAt(1) as SubCadrat;
  }

  /// The main element (the target): the one the other element is inserted into.
  get main(): SubCadrat {
    return this.getChildAt(this._target - 1) as SubCadrat;
  }

  get inserted(): SubCadrat {
    return this.getChildAt(2 - this._target) as SubCadrat;
  }

  /// Which argument is the target: 1 (the first one) or 2 (the second one).
  get target(): 1 | 2 {
    return this._target;
  }

  set target(target: 1 | 2) {
    if (target !== 1 && target !== 2) throw new Error("insert target must be 1 or 2, not " + target);
    this._target = target;
    this.notifyModification();
  }

  get direction(): InsertionDirection {
    return this._direction;
  }

  set direction(d: InsertionDirection) {
    this._direction = d;
    this.notifyModification();
  }

  /// Horizontal enlargement of the main element's frame, in percent of the inserted element's width.
  get dw(): number {
    return this._dw;
  }

  set dw(dw: number) {
    this._dw = Math.max(0, dw);
    this.notifyModification();
  }

  /// Vertical enlargement of the main element's frame, in percent of the inserted element's height.
  get dh(): number {
    return this._dh;
  }

  set dh(dh: number) {
    this._dh = Math.max(0, dh);
    this.notifyModification();
  }

  override containsOnlyOneSign(): boolean {
    return false;
  }

  deepCopy(): Insert {
    return new Insert(this._direction, this.first.deepCopy(), this.second.deepCopy(), this._target, this._dw, this._dh);
  }
}

/// A kerning operator, `%hkern` or `%vkern` (`jsesh.model.Kerning`): the groups before and after
/// it are moved closer, so that they are about one inter-sign space apart where their signs are
/// closest.
///
/// A kerning operator is a group of its own, without content. It is found:
///
/// - in a horizontal group: `p*%hkern*q`;
/// - alone in a line of a quadrat: `p:%vkern:q`;
/// - alone in a quadrat, between quadrats: `p-%hkern-q`.
///
/// It only acts in a context of its own axis (see [KerningAxis]); elsewhere, it is ignored, and
/// the text is drawn as if it was not there.
///
/// The options (`%hkern[...]`) are not used yet; they are kept as written, so that they are
/// saved back.
export class Kerning extends InnerGroup {
  /// @param options the options, as written between the brackets, without them (empty if none).
  constructor(
    readonly axis: KerningAxis,
    readonly options = "",
  ) {
    super();
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitKerning(this);
  }

  deepCopy(): Kerning {
    return new Kerning(this.axis, this.options);
  }
}

/// `before^^^sign&&&after`.
export class ComplexLigature extends HorizontalListElement {
  private readonly hasBeforeGroup: boolean;
  private readonly hasAfterGroup: boolean;

  constructor(before: InnerGroup | null, hieroglyph: Hieroglyph, after: InnerGroup | null) {
    super();
    this.hasBeforeGroup = before !== null;
    this.hasAfterGroup = after !== null;
    if (before !== null) this.addChild(before);
    this.addChild(hieroglyph);
    if (after !== null) this.addChild(after);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitComplexLigature(this);
  }

  get beforeGroup(): InnerGroup | null {
    return this.hasBeforeGroup ? (this.getChildAt(0) as InnerGroup) : null;
  }

  get hieroglyph(): Hieroglyph {
    return this.getChildAt(this.hasBeforeGroup ? 1 : 0) as Hieroglyph;
  }

  get afterGroup(): InnerGroup | null {
    return this.hasAfterGroup ? (this.getChildAt(this.hasBeforeGroup ? 2 : 1) as InnerGroup) : null;
  }

  override containsOnlyOneSign(): boolean {
    return false;
  }

  deepCopy(): ComplexLigature {
    return new ComplexLigature(
      this.beforeGroup?.deepCopy() ?? null,
      this.hieroglyph.deepCopy(),
      this.afterGroup?.deepCopy() ?? null,
    );
  }
}

/// A list of basic items, content of cartouches, sub-cadrats and philology.
export class BasicItemList extends ModelElement {
  constructor(items: Iterable<BasicItem> = []) {
    super();
    for (const i of items) this.addBasicItem(i);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitBasicItemList(this);
  }

  get items(): readonly BasicItem[] {
    return this.children as readonly BasicItem[];
  }

  addBasicItem(item: BasicItem): void {
    this.addChild(item);
  }

  insertItemsAt(index: number, items: readonly BasicItem[]): void {
    items.forEach((item, i) => this.addChildAt(index + i, item));
  }

  removeItems(start: number, end: number): BasicItem[] {
    return this.removeChildren(start, end) as BasicItem[];
  }

  deepCopy(): BasicItemList {
    const copy = new BasicItemList();
    this.copyChildrenTo(copy);
    return copy;
  }
}

/// `( ... )`: a group of basic items used inside a quadrat.
export class SubCadrat extends InnerGroup {
  constructor(content: BasicItemList = new BasicItemList()) {
    super();
    this.addChild(content);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitSubCadrat(this);
  }

  get basicItemList(): BasicItemList {
    return this.getChildAt(0) as BasicItemList;
  }

  deepCopy(): SubCadrat {
    return new SubCadrat(this.basicItemList.deepCopy());
  }
}

export class Cartouche extends InnerGroup {
  constructor(
    public type: CartoucheType,
    public startPart: CartouchePart,
    public endPart: CartouchePart,
    content: BasicItemList = new BasicItemList(),
  ) {
    super();
    this.addChild(content);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitCartouche(this);
  }

  get basicItemList(): BasicItemList {
    return this.getChildAt(0) as BasicItemList;
  }

  override containsOnlyOneSign(): boolean {
    return false;
  }

  deepCopy(): Cartouche {
    return new Cartouche(this.type, this.startPart, this.endPart, this.basicItemList.deepCopy());
  }
}

/// Philological brackets as groups (tksesh only; JSesh reads them as signs).
export class Philology extends InnerGroup {
  constructor(
    public type: number,
    content: BasicItemList = new BasicItemList(),
  ) {
    super();
    this.addChild(content);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitPhilology(this);
  }

  get basicItemList(): BasicItemList {
    return this.getChildAt(0) as BasicItemList;
  }

  override containsOnlyOneSign(): boolean {
    return false;
  }

  deepCopy(): Philology {
    return new Philology(this.type, this.basicItemList.deepCopy());
  }
}

// ---- Text

/// Converts the non-standard "Utrecht font" transliteration encoding into
/// MdC (`TransliterationUtilities.convertWindowsTranslitteration`).
export function convertWindowsTransliteration(entry: string): string {
  return entry
    .replaceAll("!", "^h")
    .replaceAll("@", "^H")
    .replaceAll("#", "^x")
    .replaceAll("£", "^X")
    .replaceAll("%", "^s")
    .replaceAll("&", "^t")
    .replaceAll("ß", "^T")
    .replaceAll("*", "^T")
    .replaceAll('"', "^d")
    .replaceAll("c", "s");
}

/// One character of alphabetic text. A MdC text block `+lsome text+s` gives
/// one element per character.
export class AlphabeticCharacter extends BasicItem {
  readonly script: ScriptCode;
  readonly uppercase: boolean;

  constructor(
    /// The letter after `+` (kept as is for unknown scripts).
    readonly mdcScriptCode: string,
    /// The character; for transliteration, the MdC letter without `^`.
    readonly codePoint: number,
    uppercase = false,
  ) {
    super();
    this.script = scriptForMdcCode(mdcScriptCode);
    this.uppercase = uppercase && this.script === "TRANSLITERATION";
  }

  /// Splits the content of a MdC text block into characters.
  static fromMdcText(mdcScriptCode: string, text: string): AlphabeticCharacter[] {
    const result: AlphabeticCharacter[] = [];
    const transliteration = scriptForMdcCode(mdcScriptCode) === "TRANSLITERATION";
    if (transliteration) text = convertWindowsTransliteration(text);
    let nextIsUpper = false;
    for (const ch of text) {
      let c = ch.codePointAt(0)!;
      if (transliteration && ch === "^") {
        nextIsUpper = true;
        continue;
      }
      if (ch === "\n" || ch === "\r" || ch === "\t") c = 32;
      result.push(new AlphabeticCharacter(mdcScriptCode, c, nextIsUpper));
      nextIsUpper = false;
    }
    return result;
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitAlphabeticCharacter(this);
  }

  /// The character as written in a MdC text block, before escaping.
  get mdcText(): string {
    const s = String.fromCodePoint(this.codePoint);
    return this.uppercase ? "^" + s : s;
  }

  isSameScriptAs(other: AlphabeticCharacter): boolean {
    return this.mdcScriptCode === other.mdcScriptCode;
  }

  isSpace(): boolean {
    return /\s/u.test(String.fromCodePoint(this.codePoint));
  }

  deepCopy(): AlphabeticCharacter {
    const copy = new AlphabeticCharacter(this.mdcScriptCode, this.codePoint, this.uppercase);
    this.copyStateTo(copy);
    return copy;
  }
}

/// `++comment+s`.
export class MdcComment extends BasicItem {
  constructor(public text: string) {
    super();
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitComment(this);
  }

  deepCopy(): MdcComment {
    const copy = new MdcComment(this.text);
    this.copyStateTo(copy);
    return copy;
  }
}

/// `|text-`: a superscript text, such as a line number.
export class Superscript extends TopItem {
  constructor(public text: string) {
    super();
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitSuperscript(this);
  }

  deepCopy(): Superscript {
    const copy = new Superscript(this.text);
    this.copyStateTo(copy);
    return copy;
  }
}

/// `!` or `!=NN%`.
export class LineBreak extends TopItem {
  /// Line skip, as a percentage of the normal one.
  constructor(public spacing = 100) {
    super();
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitLineBreak(this);
  }

  override isBreak(): boolean {
    return true;
  }

  deepCopy(): LineBreak {
    const copy = new LineBreak(this.spacing);
    this.copyStateTo(copy);
    return copy;
  }
}

/// `!!`.
export class PageBreak extends TopItem {
  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitPageBreak(this);
  }

  override isBreak(): boolean {
    return true;
  }

  deepCopy(): PageBreak {
    const copy = new PageBreak();
    this.copyStateTo(copy);
    return copy;
  }
}

/// `?NN`.
export class TabStop extends TopItem {
  constructor(public stopPos: number) {
    super();
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitTabStop(this);
  }

  deepCopy(): TabStop {
    const copy = new TabStop(this.stopPos);
    this.copyStateTo(copy);
    return copy;
  }
}

/// `{lA,B}` (thin) or `{LA,B}` (wide) horizontal rule.
export class HRule extends TopItem {
  constructor(
    public type: string,
    public startPos: number,
    public endPos: number,
  ) {
    super();
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitHRule(this);
  }

  deepCopy(): HRule {
    const copy = new HRule(this.type, this.startPos, this.endPos);
    this.copyStateTo(copy);
    return copy;
  }
}

/// The value of an unset length in [ZoneParameters].
export const ZONE_UNSET = -1;

/// The settings of a [Zone] (port of `jsesh.model.ZoneParameters`). Unset
/// settings are inherited from the enclosing zone (or text): `null` for the
/// orientation and direction, [ZONE_UNSET] for lengths.
///
/// Lengths are integers, in percent of the height of a quadrat.
export interface ZoneParameters {
  readonly orientation: TextOrientation | null;
  readonly direction: TextDirection | null;
  /// The width of the zone (and, for a zone in lines, its wrapping width).
  readonly width: number;
  readonly height: number;
  readonly lineSkip: number;
  readonly columnSkip: number;
  /// The thickness of the rules between the lines (or columns), or unset for no rule.
  readonly separator: number;
  /// The maximum width of the quadrats of the zone (the width of its columns).
  readonly quadratWidth: number;
  /// The maximum height of the quadrats of the zone (the height of its lines).
  readonly quadratHeight: number;
  readonly frame: boolean;
}

/// Builds zone parameters, normalising the lengths: a size, a quadrat size or
/// a separator of 0 or less, or a negative skip, is [ZONE_UNSET].
export function zoneParameters(p: Partial<ZoneParameters> = {}): ZoneParameters {
  const positive = (n: number | undefined) => (n !== undefined && n > 0 ? n : ZONE_UNSET);
  const nonNegative = (n: number | undefined) => (n !== undefined && n >= 0 ? n : ZONE_UNSET);
  return {
    orientation: p.orientation ?? null,
    direction: p.direction ?? null,
    width: positive(p.width),
    height: positive(p.height),
    lineSkip: nonNegative(p.lineSkip),
    columnSkip: nonNegative(p.columnSkip),
    separator: positive(p.separator),
    quadratWidth: positive(p.quadratWidth),
    quadratHeight: positive(p.quadratHeight),
    frame: p.frame ?? false,
  };
}

/// Everything inherited, natural size, no frame.
export const DEFAULT_ZONE_PARAMETERS: ZoneParameters = zoneParameters();

/// A zone, `%zone[options](content)`: an area of the text with its own
/// layout (lines or columns), direction and, optionally, size.
///
/// A zone is placed in the line (or column) of its parent like a large
/// quadrat. Its only child is its [ZoneContent]. A zone has no state of its
/// own: the items of its content have.
export class Zone extends TopItem {
  constructor(
    public parameters: ZoneParameters = DEFAULT_ZONE_PARAMETERS,
    content: ZoneContent = new ZoneContent(),
  ) {
    super();
    this.addChild(content);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitZone(this);
  }

  get content(): ZoneContent {
    return this.getChildAt(0) as ZoneContent;
  }

  override containsOnlyOneSign(): boolean {
    return false;
  }

  deepCopy(): Zone {
    const copy = new Zone(this.parameters, this.content.deepCopy());
    this.copyStateTo(copy);
    return copy;
  }
}

/// The content of a [Zone]: a text of its own, laid out in lines or columns.
/// Like [HieroglyphicText], it accepts every [TopItem], other zones included.
export class ZoneContent extends ModelElement {
  constructor(items: Iterable<TopItem> = []) {
    super();
    for (const item of items) this.addChild(item);
  }

  accept<R>(v: ModelElementVisitor<R>): R {
    return v.visitZoneContent(this);
  }

  get topItems(): readonly TopItem[] {
    return this.children as readonly TopItem[];
  }

  getTopItemAt(i: number): TopItem {
    return this.getChildAt(i) as TopItem;
  }

  insertItemsAt(index: number, items: readonly TopItem[]): void {
    items.forEach((item, i) => this.addChildAt(index + i, item));
  }

  removeItems(start: number, end: number): TopItem[] {
    return this.removeChildren(start, end) as TopItem[];
  }

  override containsOnlyOneSign(): boolean {
    return false;
  }

  deepCopy(): ZoneContent {
    const copy = new ZoneContent();
    this.copyChildrenTo(copy);
    return copy;
  }
}
