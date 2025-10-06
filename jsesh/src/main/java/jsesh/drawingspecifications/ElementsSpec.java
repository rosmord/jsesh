package jsesh.drawingspecifications;

import jsesh.drawingspecifications.elements.AreaSpec;
import jsesh.drawingspecifications.elements.FramesSpec;
import jsesh.drawingspecifications.elements.GroupsSpec;
import jsesh.drawingspecifications.elements.PageSpec;
import jsesh.drawingspecifications.elements.SignSpec;

public record ElementsSpec (
    AreaSpec areaSpec,
    GroupsSpec groupsSpec,
    PageSpec pageSpec,
    SignSpec signSpec,
    FramesSpec framesSpec
){

    /** copy builder class */
    public Builder copy() {
        return new Builder(this);
    }

    /**
     * Return reasonable default for page specifications.
     * @return
     */
    public PageSpec createDefaultPageSpec() {
        return new PageSpec(
            538, // textWidth
            760, // textHeight
            3*groupsSpec.smallSkip(),  // leftMargin
            3*groupsSpec.smallSkip() 
                + framesSpec.cartoucheSpecifications().lineWidth()
                + framesSpec.cartoucheSpecifications().margin(),
            null // format           
        );
    }

    public static class Builder {
        private AreaSpec areaSpec;
        private GroupsSpec groupsSpec;
        private PageSpec pageSpec;
        private SignSpec signSpec;
        private FramesSpec framesSpec;

        public Builder(ElementsSpec specs) {
            this.areaSpec = specs.areaSpec;
            this.groupsSpec = specs.groupsSpec;
            this.pageSpec = specs.pageSpec;
            this.signSpec = specs.signSpec;
            this.framesSpec = specs.framesSpec;
        }

        public Builder areaSpec(AreaSpec areaSpec) {
            this.areaSpec = areaSpec;
            return this;
        }

        public Builder groupsSpec(GroupsSpec groupsSpec) {
            this.groupsSpec = groupsSpec;
            return this;
        }

        public Builder pageSpec(PageSpec pageSpec) {
            this.pageSpec = pageSpec;
            return this;
        }

        public Builder signSpec(SignSpec signSpec) {
            this.signSpec = signSpec;
            return this;
        }

        public Builder framesSpec(FramesSpec framesSpec) {
            this.framesSpec = framesSpec;
            return this;
        }

        public ElementsSpec build() {
            return new ElementsSpec(areaSpec, groupsSpec, pageSpec, signSpec, framesSpec);
        }
    }

}
