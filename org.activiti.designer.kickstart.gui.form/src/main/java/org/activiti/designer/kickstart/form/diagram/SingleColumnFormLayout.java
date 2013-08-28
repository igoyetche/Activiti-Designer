package org.activiti.designer.kickstart.form.diagram;

import java.util.ArrayList;
import java.util.List;

import org.activiti.designer.util.editor.KickstartFormMemoryModel;
import org.activiti.designer.util.editor.ModelHandler;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;

/**
 * Class capable of layouting elements in a single column. Allows moving of components.
 * 
 * @author Frederik Heremans
 */
public class SingleColumnFormLayout implements FormComponentLayout {

  private int leftPadding = 20;
  private int verticalSpacing = 10;

  public void relayout(ContainerShape targetContainer) {
    int yPosition = verticalSpacing;
    
    Diagram diagram = getDiagram(targetContainer);
    KickstartFormMemoryModel model = (ModelHandler.getKickstartFormMemoryModel(EcoreUtil.getURI(diagram)));
    List<FormPropertyDefinition> definitionsInNewOrder = new ArrayList<FormPropertyDefinition>();
    
    for (Shape child : targetContainer.getChildren()) {
      Graphiti.getGaService().setLocation(child.getGraphicsAlgorithm(), leftPadding, yPosition);
      yPosition = yPosition + child.getGraphicsAlgorithm().getHeight() + verticalSpacing;
      Object businessObject = model.getFeatureProvider().getBusinessObjectForPictogramElement(child);
      if(businessObject instanceof FormPropertyDefinition) {
        definitionsInNewOrder.add((FormPropertyDefinition) businessObject);
      }
    }
    
    // Set the properties list in the new order after re-layouting
    model.getFormDefinition().setFormProperties(definitionsInNewOrder);
  }
  
  protected Diagram getDiagram(ContainerShape targetContainer) {
    while(targetContainer != null) {
      if(targetContainer instanceof Diagram) {
        return (Diagram) targetContainer;
      }
      targetContainer = targetContainer.getContainer();
    }
    throw new IllegalArgumentException("Used container is not part of a diagram");
  }

  /**
   * Moves the given shape to the right location in the given container, based on the position the shape should be moved
   * to. Other shapes in the container may be moved as well.
   */
  public void moveShape(ContainerShape targetContainer, ContainerShape sourceContainer, Shape shape, int x, int y) {
    // First, make sure the target container is a valid target. if not, find first container
    // that can in the parent chain and adjust X and Y accordingly
    
    
    boolean inSameContainer = targetContainer.equals(sourceContainer);
    
    if(targetContainer.getChildren().size() == 0) {
      // First element in the container
      Graphiti.getGaService().setLocation(shape.getGraphicsAlgorithm(), leftPadding, verticalSpacing);
      targetContainer.getChildren().add(shape);
    } else if(inSameContainer && targetContainer.getChildren().size() == 1) {
      // Already added to container, re-set the initial location
      Graphiti.getGaService().setLocation(shape.getGraphicsAlgorithm(), leftPadding, verticalSpacing);
    } else {
      // Only move when the shape is not already present as the only one in the container
      Shape shapeToReplace = null;
      // Loop over all children to find the appropriate position to insert the shape
      for (Shape child : targetContainer.getChildren()) {
        if(y < child.getGraphicsAlgorithm().getY()) {
          shapeToReplace = child;
          break;
        }
      }
      
      if(shapeToReplace != null) {
        int index = targetContainer.getChildren().indexOf(shapeToReplace);
        if(inSameContainer) {
          targetContainer.getChildren().move(index, shape);
        } else {
          targetContainer.getChildren().add(index, shape);
        }
      } else {
        if(inSameContainer) {
          targetContainer.getChildren().move(targetContainer.getChildren().size() - 1, shape);
        } else {
          // Shape needs to be added as last one, no shape to replace
          targetContainer.getChildren().add(shape);
        }
      }
      
      // Finally, re-position all shapes according to their order in the container
      relayout(targetContainer);
    }
  }

  /**
   * @param verticalSpacing
   *          vertical spacing between components.
   */
  public void setVerticalSpacing(int verticalSpacing) {
    this.verticalSpacing = verticalSpacing;
  }

  /**
   * @param leftPadding
   *          padding on the left side of all components. All components are aligned to the left.
   */
  public void setLeftPadding(int leftPadding) {
    this.leftPadding = leftPadding;
  }
}